///*
// * This class helps maually calculate min Ps by DFS
// */
//
//package cs.util;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Scanner;
//
//public class XXXResultReader {
//	private boolean debug = false;
//
//	private final String value = "P";
//
//	private String levelFileName;
//
//	private String resultFile;
//
//	public XXXResultReader(String fileName, String resultFile) {
//		levelFileName = fileName;
//		this.resultFile = resultFile;
//		doAll();
//	}
//
//	private Map<Integer, Map> levelP = new HashMap<Integer, Map>();
//
//	private void readFile() {
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new FileReader(levelFileName));
//			String temp = null;
//			while ((temp = br.readLine()) != null) {
//				String[] info = temp.split(":");
//				Map pMap = new HashMap();
//				String[] pNames = info[1].split(",");
//				for (int i = 0; i < pNames.length; i++) {
//					pMap.put(pNames[i], this.value);
//				}
//				levelP.put(Integer.parseInt(info[0]), pMap);
//			}
//
//			br.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		if (debug) {
//			for (int i = 1; i <= levelP.size(); i++) {
//				Map pMap = levelP.get(i);
//				System.out.println("Level: " + i);
//				EasyDebugger.printMap(pMap);
//			}
//		}
//	}
//
//	// print the equation for a specific level
//	private String getEquation(int level) {
//		Map pMap = levelP.get(level);
//		StringBuilder sb = new StringBuilder("");
//		sb.append("              //Level  ").append(level).append("   ")
//				.append(pMap.size()).append(" Ps").append("\n");
//
//		for (Iterator iter = pMap.keySet().iterator(); iter.hasNext();) {
//			String pName = (String) iter.next();
//			sb.append(pName).append(" + ");
//		}
//		sb.delete(sb.length() - 3, sb.length()).append(";");
//
//		return sb.toString();
//	}
//
//	/*
//	 * read the result for a given level, and print the result as equations
//	 */
//	private String readResult(int level) {
//		StringBuffer sb = new StringBuffer("");
//		sb.append("//result for Level  " + level).append("\n");
//
//		Map pMap = levelP.get(level);
//
//		{
//			BufferedReader br = null;
//			try {
//				br = new BufferedReader(new FileReader(resultFile));
//
//				String tmp = null;
//				// omit the first two lines
//				br.readLine();
//				br.readLine();
//				while ((tmp = br.readLine()) != null) {
//					String[] result = tmp.split(";");
//					String key = result[0];
//					if (pMap.containsKey(key)) {
//						int value = Integer.parseInt(result[result.length - 1]);
//						sb.append(key).append(" = ").append(value).append(";")
//								.append("\n");
//						pMap.remove(key);
//					}
//
//				}
//				// if(pMap.size() > 0){
//				// EasyDebugger.printMap(pMap);
//				// EasyDebugger.exit("some Ps have no result");
//				// }
//				br.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return sb.toString();
//	}
//
//	private void doAll() {
//		this.readFile();
//
//		Scanner scan = new Scanner(System.in);
//		for (int i = 1; i <= levelP.size(); i++) {
//			System.out.println(getEquation(i));
//			String comm = scan.nextLine();
//			if (comm.equals("done")) {
//				System.out.println(readResult(i));
//			} else if (comm.equals("reverse")) {
//				if (i > 1)
//					i -= 2;
//				else
//					i -= 1;
//			} else {
//				System.out.println("unknown commond, please input again");
//				i--;
//			}
//		}
//	}
//
//	public static void main(String[] args) {
//		String fileName = args[0];
//		String resultFile = args[1];
//		new XXXResultReader(fileName, resultFile);
//	}
// }
