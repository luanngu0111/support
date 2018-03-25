package murex.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadLog {
	public static class LOGFILE {
		String EODDATE, LOGFILE, WORKFLOW, ACTION;
		Date STARTTIME, ENDTIME;
		long RUNTIME; // millsecond
		static String workflow_pattern = ".*-\\[WORKFLOW.(.*)\\]\\ .*";
		static String start_state_pattern = ".*Processing state \\[(.*)\\]";
		static String end_state_pattern = ".*State \\[(.*)\\].*";

		static String start_time_pattern = "(.*)\\-\\[.*";
		static String end_time_pattern = "(.*)\\-\\[.*";

		static String start_action_pattern = ".*Processing action \\[(.*)\\]";
		static String end_action_pattern = ".*Action \\[(.*)\\].*";

		static String start_state_string = "Processing state";
		static String end_state_string = "ended successfully";

		static String start_action_string = "Processing action";
		static String end_action_string = "executed successfully";

		public LOGFILE() {
			STARTTIME = new Date();
			ENDTIME = new Date();
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

		public long getRUNTIME() {
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

		public void setRUNTIME(long rUNTIME) {
			RUNTIME = rUNTIME;
		}

		public Date getSTARTTIME() {
			return STARTTIME;
		}

		public Date getENDTIME() {
			return ENDTIME;
		}

		public void setSTARTTIME(Date sTARTTIME) {
			if (sTARTTIME == null) {
				STARTTIME = new Date();
			} else {
				STARTTIME = sTARTTIME;
			}
		}

		public void setENDTIME(Date eNDTIME) {
			if (eNDTIME == null) {
				ENDTIME = STARTTIME;
			} else {
				ENDTIME = eNDTIME;
			}
		}

		public void setSTARTTIME(String starttime) {
			STARTTIME = new Date(starttime);
		}

		public void setENDTIME(String eNDTIME) {
			ENDTIME = new Date(eNDTIME);
		}

		public String getSTARTTIME_formatted(String format) {
			SimpleDateFormat dt = new SimpleDateFormat(format);
			return dt.format(STARTTIME);
		}

		public String getSTARTTIME_formatted() {

			SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			return dt.format(STARTTIME);
		}

		public String getENDTIME_formatted(String format) {
			SimpleDateFormat dt = new SimpleDateFormat(format);
			return dt.format(ENDTIME);
		}

		public String getENDTIME_formatted() {

			SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			return dt.format(ENDTIME);
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
			return m.group(1);

		} else {
			return "";
		}
	}

	public static LOGFILE extractInfoState(LOGFILE log, String s) {

		if (s.contains(LOGFILE.start_state_string)) {
			String pattern = LOGFILE.workflow_pattern;
			log.setWORKFLOW(matchString(s, pattern));

			pattern = LOGFILE.start_state_pattern;
			log.setACTION(matchString(s, pattern));

			pattern = LOGFILE.start_time_pattern;
			log.setSTARTTIME(matchString(s, pattern));

		} else if (s.contains(LOGFILE.end_state_string)) {
			String pattern = LOGFILE.end_state_pattern;
			log.setACTION(matchString(s, pattern));

			pattern = LOGFILE.end_time_pattern;
			log.setENDTIME(matchString(s, pattern));

		}

		return log;
	}

	public static LOGFILE extractInfoAction(LOGFILE log, String s) {

		if (s.contains(LOGFILE.start_action_string)) {
			String pattern = LOGFILE.workflow_pattern;
			log.setWORKFLOW(matchString(s, pattern));

			pattern = LOGFILE.start_action_pattern;
			log.setACTION(matchString(s, pattern));

			pattern = LOGFILE.start_time_pattern;
			log.setSTARTTIME(matchString(s, pattern));

		} else if (s.contains(LOGFILE.end_action_string)) {
			String pattern = LOGFILE.end_action_pattern;
			log.setACTION(matchString(s, pattern));

			pattern = LOGFILE.end_time_pattern;
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

	public static void execute(String inputfile, String outputfile) throws IOException {
		LinkedHashMap<String, LOGFILE> logs = new LinkedHashMap<String, ReadLog.LOGFILE>();
		String pattern = ".*(\\d{8}).*";
		String eod_date = matchString(inputfile, pattern);
		List<String> content = readFile(inputfile);
		for (String line : content) {
			LOGFILE log = new LOGFILE();
			log = extractInfoState(log, line);
			String key = log.getACTION();
			if (logs.containsKey(key)) {
				log = logs.get(key);
				log = extractInfoState(log, line);
				log.setRUNTIME(log.getENDTIME().getTime() - log.getSTARTTIME().getTime());
				logs.put(key, log);
			} else if (log.getACTION() != null && !log.getACTION().equals("")) {
				logs.put(key, log);
			}
		}

		Set<Entry<String, LOGFILE>> set = logs.entrySet();
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			fw = new FileWriter(outputfile, true);
			bw = new BufferedWriter(fw);
			bw.append("EOD Date,State,Start,End,Run time\n");
			for (Entry entry : set) {
				LOGFILE log = (LOGFILE) entry.getValue();
				System.out.println(log.getACTION() + ";" + log.getSTARTTIME_formatted() + ";"
						+ log.getENDTIME_formatted() + ";" + parseTime(log.getRUNTIME()));

				String ct = eod_date + "," + log.getACTION() + "," + log.getSTARTTIME_formatted() + ","
						+ log.getENDTIME_formatted() + "," + parseTime(log.getRUNTIME()) + "\n";

				bw.append(ct);

			}
		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			File dir = new File("resources/logs/");
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					// Do something with child

					execute(child.getAbsolutePath(), "resources/wf_eod_live_stats.csv");
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
