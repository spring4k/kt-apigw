package com.ktds.act.apigw.handler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktds.act.apigw.api.db.entity.AdmApiDply;
import com.ktds.act.apigw.common.util.CompilerUtils;
import com.ktds.act.apigw.filter.RequestHandlerFilter;
import com.ktds.act.apigw.filter.ResponseHandlerFilter;
import com.ktds.act.apigw.filter.error.ErrorHandler;
import com.ktds.act.apigw.filter.error.exception.InternalException;
import com.ktds.act.apigw.filter.error.exception.InternalRuntimeException;
import com.ktds.act.apigw.filter.handler.RouteHandler;
import com.ktds.act.apigw.filter.model.TransactionDto;
import com.ktds.act.apigw.handler.db.entity.AdmHndlrDply;
import com.ktds.act.apigw.handler.db.service.AdmHndlrDplyService;

import com.ktds.act.apigw.filter.model.AgwErrorDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Endpoint(id = "handler")
@RequiredArgsConstructor
public class HandlerFactory implements InitializingBean {
	private final ApplicationContext applicationContext;
	
	private static Map<String, List<String>> handlerGroupMap = null;
	private static Map<String, Boolean> handlerGroupBodyRefBoolMap = null;
	private static Map<String, RouteHandler> handlerMap = null;
	private static Map<String, Boolean> handlerBodyRefBoolMap = null;
	
	private static LocalDateTime lastModifiedDate = null;
	private ObjectMapper mapper = new ObjectMapper();
	

	@Autowired
	private AdmHndlrDplyService admHndlrDplyService;


	public static void process(String handlerType, ServerWebExchange exchange, TransactionDto transaction) throws InternalRuntimeException {
		String errorHandler = null;
		try {
			// API ID extraction
			AdmApiDply apiVo = transaction.getComn().getApi();
			List<String> apiHandlerList = null;
			
			// 처리 대상 핸들러 그룹 추출 
			if (handlerType.equals(RequestHandlerFilter.REQUEST_HANDLER)) {
				if (apiVo.getReqHndlr() != null) {
					apiHandlerList = apiVo.getReqHndlr();
				}
			} else if (handlerType.equals(ResponseHandlerFilter.RESPONSE_HANDLER)) {
				if (apiVo.getResHndlr() != null) {
					apiHandlerList = apiVo.getResHndlr();
				}
			}else if (handlerType.equals(ErrorHandler.ERROR_HANDLER)) {
				if (apiVo.getErrHndlr() != null) {
					errorHandler = apiVo.getErrHndlr();
				}
			}

			// Loop를 수행하면서 핸들러 기능 처리
			if (apiHandlerList != null) {
				for (String routeHandlerId : apiHandlerList) {
					RouteHandler routeHandler = handlerMap.get(routeHandlerId);
					if (!routeHandler.process(transaction)) {
						break;
					}
				}
			} else if(handlerType.equals(ErrorHandler.ERROR_HANDLER)){
				RouteHandler routeHandler = handlerMap.get(errorHandler);
				if (!routeHandler.process(transaction)) {
					transaction.setError(AgwErrorDto.INTERNAL_SERVER_ERROR);
				}
			} 

		} catch (InternalRuntimeException e) {
			log.error("Request handling processing failure", e);
			transaction.setError(AgwErrorDto.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Request handling processing failure", e);
			transaction.setError(AgwErrorDto.INTERNAL_SERVER_ERROR);
		}

		if (transaction.getErr() != null && !handlerType.equals(ErrorHandler.ERROR_HANDLER)) {
			throw new InternalRuntimeException("API handling failure");
		}
	}

	@Override
	public void afterPropertiesSet() throws InternalException {
		log.info("HandlerFactory.afterPropertiesSet - Start");
		handlerGroupMap = new ConcurrentHashMap<String, List<String>>();
		handlerGroupBodyRefBoolMap = new ConcurrentHashMap<String, Boolean>();
		handlerMap = new ConcurrentHashMap<String, RouteHandler>();
		handlerBodyRefBoolMap = new ConcurrentHashMap<String, Boolean>();
		synchronizeDB();
		log.info("HandlerFactory.afterPropertiesSet - End");
	}
	
	/**
	 * Handler synchronization
	 * @throws InternalException 
	 */
	@Scheduled(fixedDelayString = "${common.handler.schedule.dbSync}", timeUnit = TimeUnit.SECONDS)
	public void synchronizeDB() throws InternalException {
		List<AdmHndlrDply> dbHndlrList = null;

		// 마지막 동기화 시점 시간을 비교하여 데이터 동기화 처리
		if (lastModifiedDate == null) {
			// 최초 데이터 조회
			dbHndlrList = admHndlrDplyService.getAdmHndlrList("DPLY");
		} else {
			// 변경분 데이터 조회
			dbHndlrList = admHndlrDplyService.getAdmHndlrList(lastModifiedDate);
		}
	
		try {
			// 핸들러 동기화
			if(dbHndlrList != null && dbHndlrList.size() > 0) {
				setHandler(dbHndlrList);

				AdmHndlrDply lastHndlr = dbHndlrList.stream().max((o1, o2) -> o1.getDplyDt().compareTo(o2.getDplyDt())).get();
				lastModifiedDate = lastHndlr.getDplyDt();
			}
	
		} catch (Exception e) {
			log.info("hndlr or hndlrGroup load fail", e);
		}
	}
	
	/**
	 * 핸들러 설정
	 * @param hndlrList
	 * @throws InternalException
	 * @throws ClassNotFoundException
	 */
	public void setHandler(List<AdmHndlrDply> hndlrList) throws InternalException, ClassNotFoundException {

		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
		
		for(AdmHndlrDply hndlr : hndlrList) {
			if("DPLY".equals(hndlr.getDplyType())){
				if(!hndlr.getType().equals("SYS")) { // 커스텀 핸들러 로딩
					try {
						Class<?> cls = CompilerUtils.compileSource(hndlr.getCstmClassAdr(), hndlr.getCstmClassSrc());
						if(defaultListableBeanFactory.containsBeanDefinition(hndlr.getHndlrId())){
							defaultListableBeanFactory.removeBeanDefinition(hndlr.getHndlrId());
						}
						defaultListableBeanFactory.registerBeanDefinition(hndlr.getHndlrId(), BeanDefinitionBuilder.genericBeanDefinition(cls).getRawBeanDefinition());
					} catch (Exception e) {
						throw new InternalException("Custom handler load failed", e);
					}
				}
	
				// 핸들러로 정의된 Bean 로딩
				RouteHandler routeHandler = (RouteHandler) applicationContext.getBean(hndlr.getHndlrId());
				
				if (!routeHandler.init()) {
					throw new InternalException("Handler initialization failed : " + hndlr.getHndlrId());
				}
	
				log.info("HandlerFactory.setHandler - Load : " + hndlr.toString());
				handlerMap.put(hndlr.getHndlrId(), routeHandler);
	
				handlerBodyRefBoolMap.put(hndlr.getHndlrId(),hndlr.isBodyRfrn());
			}else{
				if(!hndlr.getType().equals("SYS")) { // 커스텀 핸들러 로딩
					if(defaultListableBeanFactory.containsBeanDefinition(hndlr.getHndlrId())){
						defaultListableBeanFactory.removeBeanDefinition(hndlr.getHndlrId());
					}
				}
	
				
				handlerMap.remove(hndlr.getHndlrId());
				handlerBodyRefBoolMap.remove(hndlr.getHndlrId());
				log.info("HandlerFactory.setHandler - Remove : " + hndlr.toString());
	
			}
			

			
		}
	}


	public static boolean isHandlerGroupBodyRef(String id) {
		if (handlerGroupBodyRefBoolMap.containsKey(id)) {
			return handlerGroupBodyRefBoolMap.get(id);
		}
		return false;
	}

//	/**
//	 * 커스텀 핸들러가 저장된 디렉토리의 JAR 파일 로딩
//	 * @return
//	 * @throws InternalException
//	 */
//	private ClassLoader loadJarFile() throws InternalException {
//		ClassLoader classLoader = null;
//		try {
//			List<URL> fileUrlList = new ArrayList<URL>();
//			File dir = new File(handler.getCustomHandlerDir());
//			if(dir.isDirectory()) {
//				String[] fileList = dir.list(new FilenameFilter() {
//
//					@Override
//					public boolean accept(File dir, String name) {
//						return name.endsWith(".jar");
//					}
//				});
//
//				for(String fileName : fileList) {
//					String filePath = dir.getPath().concat(File.separator).concat(fileName);
//
//					String OS = System.getProperty("os.name").toLowerCase();
//					// mac os는 파일 경로가 /로 시작해서 file:에 /를 붙이면 URL 인식을 못하는 오류가 생김
//					if(OS.indexOf("mac") >= 0){
//						fileUrlList.add(new URL("file:".concat(filePath)));
//					}else{
//						fileUrlList.add(new URL("file:/".concat(filePath)));
//					}
//
//				}
//			}
//			URL[] urls = (URL[]) fileUrlList.toArray(new URL[0]);
//			classLoader = new URLClassLoader(urls);
//		} catch (Exception e) {
//			throw new InternalException("Custom handler class load failed");
//		}
//		return classLoader;
//	}

	@ReadOperation
    public Map<String, Object> handlerFeatures() {
		Map<String, Object> hndlrInfoMap = new HashMap<>();
		hndlrInfoMap.put("handler", handlerMap.keySet());
		hndlrInfoMap.put("handlerGroup", handlerGroupMap);
        return hndlrInfoMap;
    }

}
