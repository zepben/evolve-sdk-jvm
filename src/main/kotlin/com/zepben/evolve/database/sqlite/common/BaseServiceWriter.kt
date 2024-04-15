/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.core.Name
import com.zepben.evolve.cim.iec61970.base.core.NameType
import com.zepben.evolve.database.sqlite.common.cim.CimWriter
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.extensions.typeNameAndMRID

/**
 * A base class for writing object from a [BaseService] into the database.
 *
 * @property service The [BaseService] to save to the database.
 * @property writer The [BaseServiceWriter] used to actually write the objects to the database.
 */
abstract class BaseServiceWriter(
    protected open val service: BaseService,
    protected open val writer: CimWriter
) : BaseCollectionWriter() {

    final override fun save(): Boolean =
        saveNameTypes()
            .andDoSave()

    /**
     * Save the service specific objects to the database.
     *
     * @return true if the objects were successfully saved to the database, otherwise false
     */
    protected abstract fun doSave(): Boolean

    /**
     * Helper function for chaining [saveEach] calls using the [and] operator in a more readable manner.
     *
     * @param T The type of object to save to the database.
     * @param saver The callback used to save the objects to the database. Will be called once for each object and should return true if the object is
     *   successfully saved to the database.
     *
     * @return true if all objects are successfully saved to the database, otherwise false.
     */
    protected inline fun <reified T : IdentifiedObject> Boolean.andSaveEach(noinline saver: (T) -> Boolean): Boolean =
        this and saveEach(saver)

    /**
     * Save each object of the specified type using the provided [saver].
     *
     * @param T The type of object to save to the database.
     * @param saver The callback used to save the objects to the database. Will be called once for each object and should return true if the object is
     *   successfully saved to the database.
     *
     * @return true if all objects are successfully saved to the database, otherwise false.
     */
    protected inline fun <reified T : IdentifiedObject> saveEach(noinline saver: (T) -> Boolean): Boolean {
        var status = true
        service.sequenceOf<T>().forEach { status = status && validateSave(it, saver) }
        return status
    }

    /**
     * Validate that an object is actually saved to the database, logging an error if anything goes wrong.
     *
     * @param T The type of object being saved.
     * @param it The object being saved.
     * @param saver The callback actually saving the object to the database.
     *
     * @return true if the object is successfully saved to the database, otherwise false.
     */
    protected inline fun <reified T : IdentifiedObject> validateSave(it: T, noinline saver: (T) -> Boolean): Boolean {
        return validateSave(it, saver) { e ->
            logger.error("Failed to save ${it.typeNameAndMRID()}: ${e.message}")
        }
    }

    private fun saveNameTypes(): Boolean {
        var status = true

        service.nameTypes.forEach {
            status = status and validateSave(it, writer::save)

            it.names.forEach { name ->
                status = status and validateSave(name, writer::save)
            }
        }

        return status
    }

    private fun validateSave(nameType: NameType, saver: (nameType: NameType) -> Boolean): Boolean {
        return validateSave(nameType, saver) { e ->
            logger.error("Failed to save ${nameType.javaClass.simpleName} ${nameType.name}: ${e.message}")
        }
    }

    private fun validateSave(name: Name, saver: (name: Name) -> Boolean): Boolean {
        return validateSave(name, saver) { e ->
            logger.error("Failed to save ${name.javaClass.simpleName} ${name.name}: ${e.message}")
        }
    }

    private fun Boolean.andDoSave(): Boolean =
        this and doSave()

}
