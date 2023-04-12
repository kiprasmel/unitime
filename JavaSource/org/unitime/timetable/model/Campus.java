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
package org.unitime.timetable.model;



import javax.persistence.Entity;
import javax.persistence.Table;

import java.util.List;

import org.unitime.timetable.model.base.BaseCampus;
import org.unitime.timetable.model.dao.CampusDAO;

@Entity
@Table(name = "campus")
public class Campus extends BaseCampus {
	private static final long serialVersionUID = 1L;

	public Campus() {
		super();
	}
	
	public static List<Campus> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return (hibSession == null ? CampusDAO.getInstance().getSession() : hibSession).createQuery(
				"from Campus x where x.session.uniqueId = :sessionId order by x.reference")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).list();
	}
	
	public Object clone() {
		Campus campus = new Campus();
    	campus.setExternalUniqueId(getExternalUniqueId());
    	campus.setReference(getReference());
    	campus.setLabel(getLabel());
    	return campus;
    }
}
