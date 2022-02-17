package com.mthaler.knittings.database.table

const val NULL = "NULL"
const val INTEGER = "INTEGER"
const val REAL = "REAL"
const val TEXT = "TEXT"
const val BLOB = "BLOB"

const val PRIMARY_KEY = "PRIMARY KEY"
const val NOT_NULL = "NOT NULL"
const val AUTOINCREMENT = "AUTOINCREMENT"
const val UNIQUE = "UNIQUE"

const val IF_NOT_EXISTS = "IF NOT EXISTS"

fun DEFAULT(value: Any): String = "DEFAULT $value"

fun FOREIGN_KEY(columnName: String, referenceTable: String, referenceColumn: String): String =  "FOREIGN KEY($columnName) REFERENCES $referenceTable($referenceColumn)"

