// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.

/**
 * @package   eMarking
 * @copyright 2013 Jorge Villalón <villalon@gmail.com>
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */
package cl.uai.client.data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import cl.uai.client.EMarkingConfiguration;
import cl.uai.client.EMarkingWeb;
import cl.uai.client.MarkingInterface;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This clas contains all data pertaining to a submission, including its grade, student and marker
 * 
 * @author Jorge Villalón
 *
 */
public class SubmissionGradeData {
	
	private static Logger logger = Logger.getLogger(SubmissionGradeData.class.getName());

	public static String getRegradeMotiveText(int motiveid) {
		if(EMarkingConfiguration.getRegradeMotives().get(motiveid) == null) {
			logger.fine("Empty " + motiveid);
		}
		return EMarkingConfiguration.getRegradeMotives().get(motiveid);
	}
	private int id;
	private float finalgrade;
	private float grademin;
	private float grademax;
	private String firstname;
	private String lastname;
	private int studentid;
	private String email;
	private String coursename;
	private String courseshort;
	private int courseid;
	private boolean isgraded;
	private int markerid;
	private String markerfirstname;
	private String markerlastname;
	private String markeremail;
	private String activityname;
	private int coursemoduleid;
	private String feedback;

	private String rubricname;

	private String custommarks;
	private boolean qualitycontrol;
	private Date datecreated;

	private Date datemodified;

	private boolean regraderestrictdates;
	private Date regradeopendate;
	private Date regradeclosedate;

	/** Rubric definition **/
	private SortedMap<Integer, Criterion> rubricdefinition = null;
	public String getActivityname() {
		return activityname;
	}
	
	public int getCourseid() {
		return courseid;
	}
	
	public int getCoursemoduleid() {
		return coursemoduleid;
	}
	
	public String getCoursename() {
		return coursename;
	}
	
	public String getCourseshort() {
		return courseshort;
	}
	
	public String getCustommarks() {
		return custommarks;
	}
	
	public Date getDatecreated() {
		return datecreated;
	}
	
	public Date getDatemodified() {
		return datemodified;
	}
	public String getEmail() {
		return email;
	}	

	/**
	 * @return the feedback
	 */
	public String getFeedback() {
		return feedback;
	}

	public float getFinalgrade() {
		return finalgrade;
	}
	public String getFirstname() {
		return firstname;
	}
	public float getGrademax() {
		return grademax;
	}
	public float getGrademin() {
		return grademin;
	}
	public int getId() {
		return id;
	}
	public String getLastname() {
		return lastname;
	}
	public Level getLevelById(int lvlid) {
		for(Criterion criterion : rubricdefinition.values()) {
			for(Level lvl : criterion.getLevels().values()) {
				if(lvl.getId() == lvlid)
					return lvl;
			}
		}
		return null;
	}
	public String getMarkeremail() {
		return markeremail;
	}
	public String getMarkerfirstname() {
		return markerfirstname;
	}
	public int getMarkerid() {
		return markerid;
	}
	public String getMarkerlastname() {
		return markerlastname;
	}
	/**
	 * @return the regradeclosedate
	 */
	public Date getRegradeclosedate() {
		return regradeclosedate;
	}
	/**
	 * @return the regradeopendate
	 */
	public Date getRegradeopendate() {
		return regradeopendate;
	}
	public Map<Integer, Criterion> getRubricfillings() {
		return rubricdefinition;
	}
	/**
	 * @return the rubricname
	 */
	public String getRubricname() {
		return rubricname;
	}
	public Map<Integer, Criterion> getSortedRubricfillings() {
		SortedMap<Integer, Criterion> map = new TreeMap<Integer, Criterion>();
		for(int cid : rubricdefinition.keySet()) {
			Criterion c = rubricdefinition.get(cid);
			map.put(c.getSortorder(), c);
		}
		return map;
	}
	public int getStudentid() {
		return studentid;
	}
	public boolean isIsgraded() {
		return isgraded;
	}
	/**
	 * @return the qualitycontrol
	 */
	public boolean isQualitycontrol() {
		return qualitycontrol;
	}
	/**
	 * @return the regraderestrictdates
	 */
	public boolean isRegraderestrictdates() {
		return regraderestrictdates;
	}
	public boolean isRegradingAllowed() {
		if(!regraderestrictdates)
			return true;
		
		Date now = new Date();
		if(regradeopendate.before(now) && regradeclosedate.after(now))
			return true;
		
		return false;
	}
	
	public void loadRubricFromMap() {

		EMarkingWeb.markingInterface.addLoading(false);
		
		AjaxRequest.ajaxRequest("action=getrubric", new AsyncCallback<AjaxData>() {			
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Error getting rubric from Moodle!");
				logger.severe(caught.getMessage());
				Window.alert(caught.getMessage());				
				EMarkingWeb.markingInterface.finishLoading();
			}

			@Override
			public void onSuccess(AjaxData result) {

				List<Map<String, String>> rubric = AjaxRequest.getValuesFromResult(result);

				SortedMap<Integer, Criterion> definition = new TreeMap<Integer, Criterion>();

				try {
					for(Map<String, String> criterion : rubric) {
						int criterionId = Integer.parseInt(criterion.get("id"));
						int criterionSortOrder = Integer.parseInt(criterion.get("sortorder"));
						float maxscore = Float.parseFloat(criterion.get("maxscore"));
						String criterionDescription = criterion.get("description").toString();
						rubricname = criterion.get("rubricname").toString();
						int regradeid = Integer.parseInt(criterion.get("regradeid"));
						int regradeaccepted = Integer.parseInt(criterion.get("regradeaccepted"));
						int regrademotive = Integer.parseInt(criterion.get("motive"));
						boolean markerIsAssigned = Integer.parseInt(criterion.get("markerassigned")) == 1;
						String regradecomment = criterion.get("regradecomment").toString();
						String regrademarkercomment = criterion.get("regrademarkercomment").toString();

						JSONObject obj = new JSONObject(JsonUtils.safeEval(criterion.get("levels")));
						List<Map<String, String>> levels = AjaxRequest.getValuesFromResult(obj);

						SortedMap<Integer, Level> levelsdata = new TreeMap<Integer, Level>();
						Criterion criteriondata = new Criterion(
								criterionId, 
								criterionDescription, 
								maxscore, 
								regradeid, 
								regradeaccepted, 
								levelsdata,
								criterionSortOrder);
						criteriondata.setMarkerIsAssigned(markerIsAssigned);
						criteriondata.setRegradeComment(regradecomment);
						criteriondata.setRegradeMarkerComment(regrademarkercomment);
						criteriondata.setRegrademotive(regrademotive);
						for(Map<String,String> level : levels) {
							Level levelData = new Level(
									criteriondata,
									Integer.parseInt(level.get("id").toString()), 
									level.get("description").toString(),
									Float.parseFloat(level.get("score").toString()));
							float bonus=Float.parseFloat(criterion.get("bonus").toString());
							int commentpage = Integer.parseInt(level.get("commentpage").toString());
							int markid = Integer.parseInt(level.get("commentid").toString());
							levelData.setBonus(bonus);
							levelData.setPage(commentpage);
							levelData.setMarkId(markid);
							levelsdata.put(levelData.getId(), levelData);
							
							if(markid > 0) {
								criteriondata.setSelectedLevel(levelData.getId());
							}
						}

						criteriondata.setHueColor(definition.size());
						definition.put(criteriondata.getId(), criteriondata);
					}

					rubricdefinition = definition;
				} catch(Exception e) {
					logger.severe("Error parsing json data trying to create rubric definition and fillings");
					logger.severe(e.getMessage());
					e.printStackTrace();
					Window.alert(e.getMessage());
					rubricdefinition = null;
				} finally {
					if(rubricdefinition != null)
						EMarkingWeb.markingInterface.loadInterface();
					else
						Window.alert(MarkingInterface.messages.InvalidSubmissionData());
					EMarkingWeb.markingInterface.finishLoading();
				}
			}
		});
	}
	public void setActivityname(String activityname) {
		this.activityname = activityname;
	}
	public void setCourseid(int courseid) {
		this.courseid = courseid;
	}
	public void setCoursemoduleid(int coursemoduleid) {
		this.coursemoduleid = coursemoduleid;
	}
	public void setCoursename(String coursename) {
		this.coursename = coursename;
	}
	public void setCourseshort(String courseshort) {
		this.courseshort = courseshort;
	}
	public void setCustommarks(String custommarks) {
		this.custommarks = custommarks;
	}
	public void setDatecreated(long datecreated) {
		this.datecreated = new Date(datecreated * 1000);
	}
	public void setDatemodified(long datemodified) {
		this.datemodified = new Date(datemodified * 1000);
	}
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @param feedback the feedback to set
	 */
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public void setFinalgrade(float finalgrade) {
		this.finalgrade = finalgrade;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public void setGrademax(float grademax) {
		this.grademax = grademax;
	}
	public void setGrademin(float grademin) {
		this.grademin = grademin;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setIsgraded(boolean isgraded) {
		this.isgraded = isgraded;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public void setMarkeremail(String markeremail) {
		this.markeremail = markeremail;
	}

	public void setMarkerfirstname(String markerfirstname) {
		this.markerfirstname = markerfirstname;
	}
	public void setMarkerid(int markerid) {
		this.markerid = markerid;
	}
	public void setMarkerlastname(String markerlastname) {
		this.markerlastname = markerlastname;
	}
	/**
	 * @param qualitycontrol the qualitycontrol to set
	 */
	public void setQualitycontrol(boolean qualitycontrol) {
		this.qualitycontrol = qualitycontrol;
	}
	/**
	 * @param regradeclosedate the regradeclosedate to set
	 */
	public void setRegradeclosedate(Date regradeclosedate) {
		this.regradeclosedate = regradeclosedate;
	}
	
	/**
	 * @param regradeopendate the regradeopendate to set
	 */
	public void setRegradeopendate(Date regradeopendate) {
		this.regradeopendate = regradeopendate;
	}
	
	/**
	 * @param regraderestrictdates the regraderestrictdates to set
	 */
	public void setRegraderestrictdates(boolean regraderestrictdates) {
		this.regraderestrictdates = regraderestrictdates;
	}
	
	/**
	 * @param rubricname the rubricname to set
	 */
	public void setRubricname(String rubricname) {
		this.rubricname = rubricname;
	}

	public void setStudentid(int studentid) {
		this.studentid = studentid;
	}
}