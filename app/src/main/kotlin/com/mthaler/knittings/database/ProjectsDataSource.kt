package com.mthaler.knittings.database

import com.mthaler.knittings.model.Knitting
import java.util.ArrayList

interface ProjectsDataSource {

    val allProjects: List<Knitting>

    fun addProject(project: Knitting): Knitting

    fun updateProject(project: Knitting): Knitting

    fun deleteProject(project: Knitting)

    fun deleteAllProjects()

    fun getProject(id: Long): Knitting
}