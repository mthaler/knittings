package com.mthaler.knittings.database

import com.mthaler.knittings.model.Project
import java.util.ArrayList

interface ProjectsDataSource<T : Project> {

    val allProjects: List<T>

    fun addProject(project: T, manualID: Boolean = false): T

    fun updateProject(project: T): T

    fun deleteProject(project: T)

    fun deleteAllProjects()

    fun getProject(id: Long): T
}