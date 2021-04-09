/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseInfo;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.VariableTitleCourseProvider;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class PurdueVariableTitleCourseProvider implements VariableTitleCourseProvider {
	private static Logger sLog = Logger.getLogger(PurdueVariableTitleCourseProvider.class);
	private ExternalTermProvider iExternalTermProvider;
	
	public PurdueVariableTitleCourseProvider() {
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
	}
	
	protected String getVariableTitleCourseSQL() {
		return ApplicationProperties.getProperty("purdue.vt.variableTitleCourseSQL",
				"select subj_code, crse_numb, crse_title, credit_hr_ind, credit_hr_low, credit_hr_high, gmod_code, gmod_desc, gmod_default_ind " +
				"from timetable.szgv_reg_vartl_course where " +
				"concat(concat(subj_code, ' '), crse_numb) like :query and attr_code = 'VART' and " +
				"course_effective_term <= :term and :term < course_end_term and " +
				"attr_effective_term <= :term and :term < attr_end_term and " +
				"gmod_effective_term <= :term and :term < gmod_end_term " +
				"order by subj_code, crse_numb, gmod_code");
	}
	
	protected String getInstructorNameFormat() {
		return ApplicationProperties.getProperty("purdue.vt.instructorNameFormat", "last-first-middle");
	}
	
	protected String getDisclaimer() {
		return ApplicationProperties.getProperty("purdue.vt.disclaimer", null);
	}

	@Override
	public Collection<VariableTitleCourseInfo> getVariableTitleCourses(String query, int limit, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		org.hibernate.Query q = helper.getHibSession().createSQLQuery(getVariableTitleCourseSQL());
		if (query != null && query.indexOf(" - ") >= 0)
			query = query.substring(0, query.indexOf(" - "));
		q.setText("query", query == null ? "%" : query.toUpperCase() + "%");
		q.setText("term", iExternalTermProvider.getExternalTerm(server.getAcademicSession()));
		if (limit > 0)
			q.setMaxResults(5 * limit);
		
		Map<String, VariableTitleCourseInfo> courses = new HashMap<String, VariableTitleCourseInfo>();
		
		for (Object[] line: (List<Object[]>)q.list()) {
			String subject = (String)line[0];
			String courseNbr = (String)line[1];
			String title = (String)line[2];
			String credInd = (String)line[3];
			Number credLo = (Number)line[4];
			Number credHi = (Number)line[5];
			String gmCode = (String)line[6];
			String gmDesc = (String)line[7];
			String gmInd = (String)line[8];
			VariableTitleCourseInfo info = courses.get(subject + " " + courseNbr);
			if (info == null) {
				if (limit > 0 && courses.size() >= limit) break;
				info = new VariableTitleCourseInfo();
				info.setSubject(subject);
				info.setCourseNbr(courseNbr);
				info.setTitle(title);
				info.setStartDate(server.getAcademicSession().getDefaultStartDate());
				info.setEndDate(server.getAcademicSession().getDefaultEndDate());
				if ("TO".equals(credInd)) {
					for (float credit = credLo.floatValue(); credit <= credHi.floatValue(); credit += 1f) {
						info.addAvailableCredit(credit);
					}
				} else if ("OR".equals(credInd)) {
					info.addAvailableCredit(credLo.floatValue());
					info.addAvailableCredit(credHi.floatValue());
				} else {
					info.addAvailableCredit(credLo.floatValue());
				}
				courses.put(subject + " " + courseNbr, info);
			}
			if (gmCode != null) {
				info.addGradeMode(new GradeMode(gmCode, gmDesc, false));
				if ("D".equals(gmInd))
					info.setDefaultGradeModeCode(gmCode);
			}
		}
		return new TreeSet<VariableTitleCourseInfo>(courses.values());
	}
	
	@Override
	public VariableTitleCourseInfo getVariableTitleCourse(String query, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		org.hibernate.Query q = helper.getHibSession().createSQLQuery(getVariableTitleCourseSQL());
		if (query != null && query.indexOf(" - ") >= 0)
			query = query.substring(0, query.indexOf(" - "));
		q.setText("query", query.toUpperCase());
		q.setText("term", iExternalTermProvider.getExternalTerm(server.getAcademicSession()));
		
		NameFormat nameFormat = NameFormat.fromReference(getInstructorNameFormat());

		VariableTitleCourseInfo info = null;
		for (Object[] line: (List<Object[]>)q.list()) {
			String subject = (String)line[0];
			String courseNbr = (String)line[1];
			String title = (String)line[2];
			String credInd = (String)line[3];
			Number credLo = (Number)line[4];
			Number credHi = (Number)line[5];
			String gmCode = (String)line[6];
			String gmDesc = (String)line[7];
			String gmInd = (String)line[8];
			if (info == null) {
				info = new VariableTitleCourseInfo();
				info.setSubject(subject);
				info.setCourseNbr(courseNbr);
				info.setTitle(title);
				info.setStartDate(server.getAcademicSession().getDefaultStartDate());
				info.setEndDate(server.getAcademicSession().getDefaultEndDate());
				if ("TO".equals(credInd)) {
					for (float credit = credLo.floatValue(); credit <= credHi.floatValue(); credit += 1f) {
						info.addAvailableCredit(credit);
					}
				} else if ("OR".equals(credInd)) {
					info.addAvailableCredit(credLo.floatValue());
					info.addAvailableCredit(credHi.floatValue());
				} else {
					info.addAvailableCredit(credLo.floatValue());
				}
				for (DepartmentalInstructor di: (List<DepartmentalInstructor>)helper.getHibSession().createQuery(
						"select i from DepartmentalInstructor i inner join i.department.subjectAreas sa where " +
						"i.department.session = :sessionId and sa.subjectAreaAbbreviation = :subject and i.externalUniqueId is not null"
						).setCacheable(true).setLong("sessionId", server.getAcademicSession().getUniqueId()).setString("subject", subject).list()) {
					info.addInstructor(di.getUniqueId(), nameFormat.format(di));
				}
				
				if (Customization.CourseDetailsProvider.hasProvider()) {
					CourseDetailsProvider dp = Customization.CourseDetailsProvider.getProvider();
					try {
						info.setDetails(dp.getDetails(server.getAcademicSession(), subject, courseNbr));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				info.setDisclaimer(getDisclaimer());
			}
			if (gmCode != null) {
				info.addGradeMode(new GradeMode(gmCode, gmDesc, false));
				if ("D".equals(gmInd))
					info.setDefaultGradeModeCode(gmCode);
			}
		}
		return info;
	}

	@Override
	public void dispose() {
	}
}