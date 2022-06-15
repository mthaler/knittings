package com.mthaler.knittings.database

import com.mthaler.knittings.model.Knitting
import java.util.ArrayList

interface ProjectsDataSource {

    val allProjects: ArrayList<Knitting>

    fun addProject(project: Knitting, manualID: Boolean = false): Knitting

    fun updateProject(project: Knitting): Knitting

    fun deleteProject(project: Knitting)

    fun deleteAllProjects()

    fun getProject(id: Long): Knitting
}