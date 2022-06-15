package com.mthaler.knittings.database

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Project
import java.util.ArrayList

interface ProjectsDataSource<T : Project> {

    val allProjects: ArrayList<Knitting>

    fun addProject(project: T, manualID: Boolean = false): T

    fun updateProject(project: T): T

    fun deleteProject(project: Knitting)

    fun deleteAllProjects()

    fun getProject(id: Long): T
}