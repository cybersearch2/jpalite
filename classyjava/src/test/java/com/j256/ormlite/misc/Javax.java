/**
ISC License (https://opensource.org/licenses/ISC)

Copyright 2021, Gray Watson

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. */
package com.j256.ormlite.misc;

import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity(name = "notjavax")
public class Javax {

	public static final String STUFF_FIELD_NAME = "notstuff";
	public static final String MAPPED_BY_FIELD_NAME = "notmappedby";
	public static final String JOIN_FIELD_NAME = "notjoinfield";
	public static final String JAVAX_ENTITY_NAME = "notjavax";
	public static final String COLUMN_DEFINITION = "column definition";

	public Javax() {
	}

	@Id
	@GeneratedValue
	public int generatedId;
	@Id
	public int id;
	@Column(name = STUFF_FIELD_NAME)
	public String stuff;
	// this thing is not serializable
	@Column
	public Javax unknown;
	@ManyToOne
	Foreign foreignManyToOne;
	@OneToOne
	Foreign foreignOneToOne;
	@OneToMany
	Collection<Foreign> foreignOneToMany;
	@OneToMany(mappedBy = MAPPED_BY_FIELD_NAME)
	Collection<Foreign> mappedByField;
	@ManyToOne
	@JoinColumn(name = JOIN_FIELD_NAME)
	Foreign joinFieldName;
	@Column(columnDefinition = COLUMN_DEFINITION)
	String columnDefinition;
	@Column(unique = true)
	String uniqueColumn;
	@Column(nullable = false)
	String nullableColumn;
	@ManyToOne
	@JoinColumn(unique = true)
	String uniqueJoinColumn;
	@ManyToOne
	@JoinColumn(nullable = false)
	String nullableJoinColumn;
	@Enumerated
	OurEnum ourEnumOrdinal;
	@Enumerated(EnumType.STRING)
	OurEnum ourEnumString;
	@Version
	int version;
	@Basic
	int basic;
	@Basic(optional = false)
	String basicNotOptional;
}
