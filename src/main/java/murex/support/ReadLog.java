package murex.support;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadLog {
	public static class LOGFILE {
		String EODDATE, LOGFILE, WORKFLOW, ACTION;
		Date STARTTIME, ENDTIME;
		int RUNTIME; // minutes

		public LOGFILE() {

		}

		public String getEODDATE() {
			return EODDATE;
		}

		public String getLOGFILE() {
			return LOGFILE;
		}

		public String getWORKFLOW() {
			return WORKFLOW;
		}

		public String getACTION() {
			return ACTION;
		}

		public int getRUNTIME() {
			return RUNTIME;
		}

		public void setEODDATE(String eODDATE) {
			EODDATE = eODDATE;
		}

		public void setLOGFILE(String lOGFILE) {
			LOGFILE = lOGFILE;
		}

		public void setWORKFLOW(String wORKFLOW) {
			WORKFLOW = wORKFLOW;
		}

		public void setACTION(String aCTION) {
			ACTION = aCTION;
		}

		public void setRUNTIME(int rUNTIME) {
			RUNTIME = rUNTIME;
		}

		public Date getSTARTTIME() {
			return STARTTIME;
		}

		public Date getENDTIME() {
			return ENDTIME;
		}

		public void setSTARTTIME(Date sTARTTIME) {
			STARTTIME = sTARTTIME;
		}

		public void setENDTIME(Date eNDTIME) {
			ENDTIME = eNDTIME;
		}

		public void setSTARTTIME(String starttime) {
			STARTTIME = new Date(starttime);
		}

		public void setENDTIME(String eNDTIME) {
			ENDTIME = new Date(eNDTIME);
		}

	}

	public static List<String> readFile(String path) {
		BufferedReader br = null;
		FileReader fr = null;
		List<String> result = new ArrayList<String>();

		try {

			// br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(path);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				result.add(sCurrentLine);
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
		return result;

	}

	public static String matchString(String s, String pattern) {
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(s);
		if (m.find()) {
			System.out.println("Found value: " + m.group(1));
			return m.group(1);

		} else {
			System.out.println("NO MATCH");
			return "";
		}
	}

	public static LOGFILE extractInfo(LOGFILE log, String s) {

		if (s.contains("Processing state")) {
			String pattern = ".*-\\[WORKFLOW.(.*)\\]\\ .*";
			log.setWORKFLOW(matchString(s, pattern));

			pattern = ".*Processing state \\[(.*)\\]";
			log.setACTION(matchString(s, pattern));

			pattern = "(.*)\\-\\[.*";
			log.setSTARTTIME(matchString(s, pattern));

		} else if (s.contains("executed successfully")) {
			String pattern = ".*Processing state \\[(.*)\\]";
			log.setACTION(matchString(s, pattern));

			pattern = "(.*)\\-\\[.*";
			log.setENDTIME(matchString(s, pattern));

		}

		return log;
	}

	public static String parseTime(long time) {
		String format = "%d:%d:%d";
		time = time / 1000;
		long hour = time / 3600;
		time = time % 3600;
		long min = time / 60;
		time = time % 60;
		long sec = time;

		return String.format(format, hour, min, sec);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, LOGFILE> logs = new HashMap<String, ReadLog.LOGFILE>();
		List<String> content = readFile("resources/logs/mx_launch_workflow.pl_20180302_234925_wf_eod_live.log");
		for (String line : content) {
			LOGFILE log = new LOGFILE();
			log = extractInfo(log, line);
			String key = log.getACTION();
			if (logs.containsKey(key)) {
				log = logs.get(key);
				log = extractInfo(log, line);
				logs.put(key, log);
				System.out.println(log.getACTION() + " " + log.getSTARTTIME() + "  " + log.getENDTIME());
			} else if (log.getACTION() != null && !log.getACTION().equals("")) {
				logs.put(key, log);
				System.out.println(log.getACTION() + " " + log.getSTARTTIME() + "  " + log.getENDTIME());
			}
		}
	}

}
