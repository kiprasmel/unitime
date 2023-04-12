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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseDatePattern implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iPattern;
	private Integer iOffset;
	private Integer iType;
	private Boolean iVisible;
	private Float iNumberOfWeeks;

	private Session iSession;
	private Set<DatePattern> iParents;
	private Set<Department> iDepartments;

	public BaseDatePattern() {
	}

	public BaseDatePattern(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "date_pattern_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "date_pattern_seq")
	})
	@GeneratedValue(generator = "date_pattern_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = true, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "pattern", nullable = false, length = 366)
	public String getPattern() { return iPattern; }
	public void setPattern(String pattern) { iPattern = pattern; }

	@Column(name = "offset", nullable = false, length = 4)
	public Integer getOffset() { return iOffset; }
	public void setOffset(Integer offset) { iOffset = offset; }

	@Column(name = "type", nullable = true, length = 2)
	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	@Column(name = "visible", nullable = true)
	public Boolean isVisible() { return iVisible; }
	@Transient
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	@Column(name = "nr_weeks", nullable = true)
	public Float getNumberOfWeeks() { return iNumberOfWeeks; }
	public void setNumberOfWeeks(Float numberOfWeeks) { iNumberOfWeeks = numberOfWeeks; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToMany
	@JoinTable(name = "date_pattern_parent",
		joinColumns = { @JoinColumn(name = "date_pattern_id") },
		inverseJoinColumns = { @JoinColumn(name = "parent_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<DatePattern> getParents() { return iParents; }
	public void setParents(Set<DatePattern> parents) { iParents = parents; }
	public void addToparents(DatePattern datePattern) {
		if (iParents == null) iParents = new HashSet<DatePattern>();
		iParents.add(datePattern);
	}

	@ManyToMany(mappedBy = "datePatterns")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DatePattern)) return false;
		if (getUniqueId() == null || ((DatePattern)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DatePattern)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "DatePattern["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "DatePattern[" +
			"\n	Name: " + getName() +
			"\n	NumberOfWeeks: " + getNumberOfWeeks() +
			"\n	Offset: " + getOffset() +
			"\n	Pattern: " + getPattern() +
			"\n	Session: " + getSession() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
