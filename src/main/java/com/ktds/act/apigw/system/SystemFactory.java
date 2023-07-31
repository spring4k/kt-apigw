package com.ktds.act.apigw.system;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.ktds.act.apigw.filter.error.exception.InternalException;
import com.ktds.act.apigw.filter.model.Prot;
import com.ktds.act.apigw.filter.router.SocketRouter;
import com.ktds.act.apigw.handler.db.entity.AdmHndlrDply;
import com.ktds.act.apigw.system.config.PortalSystem;
import com.ktds.act.apigw.system.db.entity.AdmSys;
import com.ktds.act.apigw.system.db.service.AdmSysService;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Endpoint(id = "system")
public class SystemFactory implements InitializingBean  {
	private static Map<String, AdmSys> systemMap = null;
	private static LocalDateTime lastModifiedDate = null;
	@Autowired
	private AdmSysService admSysService;

	@Autowired
	private PortalSystem portalSystem;

	private List<String> portalSystemIdList;

	/**
	 * System synchronization
	 */
	@Scheduled(fixedDelayString = "${common.system.schedule.dbSync}", timeUnit = TimeUnit.SECONDS)
	public void synchronizeDB() {
		List<AdmSys> dbSystemList = null;

		// 마지막 동기화 시점 시간을 비교하여 데이터 동기화 처리
		LocalDateTime nowTime = LocalDateTime.now();
		if (lastModifiedDate == null) {
			dbSystemList = admSysService.getAdmSysList("DPLY");
		} else {
			dbSystemList = admSysService.getAdmSysList(lastModifiedDate);
		}
		// 이전 DB 동기화 데이터 건수
		int oldTotCnt = systemMap.size();
		int newSystemCnt = 0;

		if(dbSystemList != null && dbSystemList.size() > 0) {
			// System 정보를 ENTITY MAP에 현행화
			for (AdmSys systemEntity : dbSystemList) {
				if("DPLY".equals(systemEntity.getDplyType())){
					// 추가된 system 갯수
					// if(!systemMap.containsKey(systemEntity.getSysId())){
					// 	newSystemCnt++;
					// }
					systemMap.put(systemEntity.getSysId(), systemEntity);
					// TODO 프로토콜이 Socket의 경우 초기화(등록/수정) 메소드 호출
					// if(Prot.SOCKET.getValue().equals(systemEntity.getEdpt().getProt())) {
					// 	초기화 메소드(systemEntity.getSysId(), systemEntity.getEdpt().getAtrib());
					// }
					// protocol 이 SOCKET 일 경우만 SoccketFactory 갱신
					if(systemEntity.getEdpt().getProt().equals(Prot.SOCKET.getValue())){
//						socketHelperFactory.addHelper(systemEntity.getSysId(), systemEntity.getEdpt().getAtrib());
						SocketRouter.create(systemEntity.getSysId(), systemEntity.getEdpt().getAtrib());
					}

					log.info("SystemFactory.synchronizeDB - Load : " + systemEntity.toString());
				}else{
					systemMap.remove(systemEntity.getSysId());


					// TODO 프로토콜이 Socket의 경우 제거 메소드 호출
					// if(Prot.SOCKET.getValue().equals(systemEntity.getEdpt().getProt())) {
					// 	제거 메소드(systemEntity.getSysId());
					// }
					// protocol 이 SOCKET 일 경우만 SoccketFactory 갱신
					if(systemEntity.getEdpt().getProt().equals(Prot.SOCKET.getValue())){
//						socketHelperFactory.removeHelper(systemEntity.getSysId());
						SocketRouter.close(systemEntity.getSysId());
					}
					log.info("SystemFactory.synchronizeDB - Remove : " + systemEntity.toString());
				}
//				// 마지막 동기화 시간 현행화
//				if (lastModifiedDate == null || (systemEntity.getUpdDt() != null && systemEntity.getUpdDt().isAfter(lastModifiedDate))) {
//					lastModifiedDate = nowTime;
//				}
			}

			// 마지막 동기화 시간 현행화
			AdmSys lastSys = dbSystemList.stream().max((o1, o2) -> o1.getDplyDt().compareTo(o2.getDplyDt())).get();
			lastModifiedDate = lastSys.getDplyDt();

		}
		// 최신 DB 동기화 데이터 건수
		// removeDeletedSys(oldTotCnt, newSystemCnt);
	}

	/**
	 * DB Loading
	 */
	@Override
	public void afterPropertiesSet() throws InternalException {
		log.info("SystemFactory.afterPropertiesSet - Start");

		systemMap = new ConcurrentHashMap<String, AdmSys>();
		portalSystemIdList = new ArrayList<>();

		// Application.yaml에 정의된 시스템 동기화
		List<AdmSys> systemEntity = portalSystem.getSystem();
		for(AdmSys portalSystem : systemEntity){
			systemMap.put(portalSystem.getSysId(), portalSystem);
			portalSystemIdList.add(portalSystem.getSysId());
			log.info("Portal System synchronization : " + portalSystem.toString());
		}

		synchronizeDB();

		log.info("SystemFactory.afterPropertiesSet - End");
	}

	/**
	 * 시스템 정보 반환
	 *
	 * @param id
	 * @return api
	 */
	public static AdmSys getSystem(String id) {
		AdmSys system = null;
		if (systemMap.containsKey(id)) {
			system = systemMap.get(id);
		}
		return system;
	}

	@ReadOperation
    public Map<String, AdmSys> systemFeatures() {
        return systemMap;
    }

	private void removeDeletedSys(int oldTotCnt, int newSystemCnt) {
		int totCnt = admSysService.count().intValue();
		int deleteSystemCnt = oldTotCnt+newSystemCnt-totCnt-portalSystemIdList.size();
		if(oldTotCnt>portalSystemIdList.size() && deleteSystemCnt>0){
			Set<String> newSysKeySet = new HashSet<>(admSysService.getAdmSysId());
			Set<String> oldSysKeySet = new HashSet<>(systemMap.keySet());
			oldSysKeySet.removeAll(newSysKeySet);
			for(String deletedKey: oldSysKeySet){
				if(!portalSystemIdList.contains(deletedKey)){
					systemMap.remove(deletedKey);
					log.info("SystemFactory.synchronizeDB - remove system : " + deletedKey);
				}
			}
		}
	}
}
