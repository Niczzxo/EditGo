package io.github.niczzxo.editgo.data

import io.github.niczzxo.editgo.domain.Project

/** v1: in-memory only. Projects do NOT survive an app restart yet —
 *  persistence (Room/DataStore) is a v2 task, see README. */
interface ProjectRepository {
    fun list(): List<Project>
    fun add(project: Project): Project
    fun remove(id: String)
}

class InMemoryProjectRepository : ProjectRepository {
    private val projects = mutableListOf<Project>()

    override fun list(): List<Project> = projects.toList()

    override fun add(project: Project): Project {
        projects.removeAll { it.id == project.id }
        projects.add(0, project)
        return project
    }

    override fun remove(id: String) {
        projects.removeAll { it.id == id }
    }
}
