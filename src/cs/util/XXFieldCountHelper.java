//package cs.util;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
//
//public class XXFieldCountHelper {
//	private static final Object available = new Object();
//	private static Map<String, Object> fieldValid = new HashMap(50000);
//	private static Map<String, Object> fieldAccess = new HashMap(10000);
//	private static Map<String, String> refreshToAlias = new HashMap(50000);
//	
//	public static void refreshTrack(String oldName, String refreshedName){
//		refreshToAlias.put(refreshedName, oldName);
//		if(!fieldValid.containsKey(oldName)){
//			fieldValid.put(oldName, available);
//		}
//	}
//	
//	public static void refreshTrackForFieldAccess(String oldName, String refreshedName){
//		fieldAccess.put(oldName, available);
//		refreshTrack(oldName, refreshedName);
//		
//	}
//	
//	/*
//	 * return the # of local field based on class level
//	 */
//	public static int checkField(Map<String, Integer> resultMap, Set fieldSet){
////		EasyDebugger.message("field count: " + fieldValid.size());
////		EasyDebugger.printMap(fieldValid);
////		EasyDebugger.exit("");
//		
//		
//		int fCount = 0;
//		
//		int tmpValue;
//		int missed = 0;
//		String aliasName;
//		String fieldName;
//		String fieldP;
//		for (Iterator iter = fieldSet.iterator(); iter.hasNext();) {
//			fieldName = (String) iter.next();
//			fieldP = fieldName + "#P";
//			if(!resultMap.containsKey(fieldP)){
//				missed++;
//				continue;
//			}
//			tmpValue = resultMap.get(fieldP);
//			
//			if(!refreshToAlias.containsKey(fieldName)){
//				EasyDebugger.exit("");
//			}
//			aliasName = refreshToAlias.get(fieldName);
//			if(tmpValue == 0 && fieldValid.containsKey(aliasName) && !fieldAccess.containsKey(aliasName))
//				fCount++;
//			if(tmpValue > 0)
//				fieldValid.remove(aliasName);
//		}//end outter for
//		
//		System.out.println("local field count on class level: missed " + missed);
//		
//		return fCount;
//	}
//	
// }
