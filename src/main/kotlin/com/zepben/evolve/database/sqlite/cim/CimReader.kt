/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim

import com.zepben.evolve.cim.iec61968.common.Document
import com.zepben.evolve.cim.iec61968.common.Organisation
import com.zepben.evolve.cim.iec61968.common.OrganisationRole
import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.core.Name
import com.zepben.evolve.cim.iec61970.base.core.NameType
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableDocuments
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableOrganisationRoles
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableOrganisations
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableIdentifiedObjects
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableNameTypes
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableNames
import com.zepben.evolve.database.sqlite.common.DuplicateMRIDException
import com.zepben.evolve.database.sqlite.common.DuplicateNameTypeException
import com.zepben.evolve.database.sqlite.extensions.getInstant
import com.zepben.evolve.database.sqlite.extensions.getNullableString
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.exceptions.UnsupportedIdentifiedObjectException
import com.zepben.evolve.services.common.extensions.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException

/**
 * A base class for reading CIM objects from a database.
 *
 * @param service The [BaseService] used to store any items read from the database.
 * @property logger The [Logger] to use for this reader.
 */
abstract class CimReader(
    protected open val service: BaseService
) {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    // ###################
    // # IEC61968 Common #
    // ###################

    /**
     * Populate the [Document] fields from [TableDocuments].
     *
     * @param document The [Document] instance to populate.
     * @param table The database table to read the [Document] fields from.
     * @param resultSet The record in the database table containing the fields for this [Document].
     *
     * @return true if the [Document] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    protected fun loadDocument(document: Document, table: TableDocuments, resultSet: ResultSet): Boolean {
        document.apply {
            title = resultSet.getString(table.TITLE.queryIndex).emptyIfNull().internEmpty()
            createdDateTime = resultSet.getInstant(table.CREATED_DATE_TIME.queryIndex)
            authorName = resultSet.getString(table.AUTHOR_NAME.queryIndex).emptyIfNull().internEmpty()
            type = resultSet.getString(table.TYPE.queryIndex).emptyIfNull().internEmpty()
            status = resultSet.getString(table.STATUS.queryIndex).emptyIfNull().internEmpty()
            comment = resultSet.getString(table.COMMENT.queryIndex).emptyIfNull().internEmpty()
        }

        return loadIdentifiedObject(document, table, resultSet)
    }

    /**
     * Create an [Organisation] and populate its fields from [TableOrganisations].
     *
     * @param table The database table to read the [Organisation] fields from.
     * @param resultSet The record in the database table containing the fields for this [Organisation].
     * @param setIdentifier A callback to register the mRID of this [Organisation] for logging purposes.
     *
     * @return true if the [Organisation] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableOrganisations, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val organisation = Organisation(setIdentifier(resultSet.getString(table.MRID.queryIndex)))

        return loadIdentifiedObject(organisation, table, resultSet) && service.addOrThrow(organisation)
    }

    /**
     * Create a [NameType] and populate its fields from [TableNameTypes].
     *
     * @param table The database table to read the [NameType] fields from.
     * @param resultSet The record in the database table containing the fields for this [NameType].
     * @param setLastNameType A callback to register the name of this [NameType] for logging purposes.
     *
     * @return true if the [NameType] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableNameTypes, resultSet: ResultSet, setLastNameType: (String) -> String): Boolean {
        val nameType = NameType(setLastNameType(resultSet.getString(table.NAME.queryIndex))).apply {
            description = resultSet.getString(table.DESCRIPTION.queryIndex)
        }

        return service.addOrThrow(nameType)
    }

    /**
     * Create a [Name] and populate its fields from [TableNames].
     *
     * @param table The database table to read the [Name] fields from.
     * @param resultSet The record in the database table containing the fields for this [Name].
     * @param setLastName A callback to register the name of this [Name] for logging purposes.
     *
     * @return true if the [Name] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableNames, resultSet: ResultSet, setLastName: (String) -> String): Boolean {
        val nameTypeName = resultSet.getString(table.NAME_TYPE_NAME.queryIndex)
        val nameName = resultSet.getString(table.NAME.queryIndex)
        setLastName("$nameTypeName:$nameName")

        val nameType = service.getNameTypeOrThrow(nameTypeName)
        service.getOrThrow<IdentifiedObject>(resultSet.getString(table.IDENTIFIED_OBJECT_MRID.queryIndex), "Name $nameName [$nameTypeName]")
            .addName(nameType, nameName)

        return true
    }

    /**
     * Populate the [OrganisationRole] fields from [TableOrganisationRoles].
     *
     * @param organisationRole The [OrganisationRole] instance to populate.
     * @param table The database table to read the [OrganisationRole] fields from.
     * @param resultSet The record in the database table containing the fields for this [OrganisationRole].
     *
     * @return true if the [OrganisationRole] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    protected fun loadOrganisationRole(organisationRole: OrganisationRole, table: TableOrganisationRoles, resultSet: ResultSet): Boolean {
        organisationRole.apply {
            organisation = service.ensureGet(
                resultSet.getNullableString(table.ORGANISATION_MRID.queryIndex),
                typeNameAndMRID()
            )
        }

        return loadIdentifiedObject(organisationRole, table, resultSet)
    }

    // ######################
    // # IEC61970 Base Core #
    // ######################

    /**
     * Populate the [IdentifiedObject] fields from [TableIdentifiedObjects].
     *
     * @param identifiedObject The [IdentifiedObject] instance to populate.
     * @param table The database table to read the [IdentifiedObject] fields from.
     * @param resultSet The record in the database table containing the fields for this [IdentifiedObject].
     *
     * @return true if the [IdentifiedObject] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    protected fun loadIdentifiedObject(identifiedObject: IdentifiedObject, table: TableIdentifiedObjects, resultSet: ResultSet): Boolean {
        identifiedObject.apply {
            name = resultSet.getString(table.NAME.queryIndex).emptyIfNull().internEmpty()
            description = resultSet.getString(table.DESCRIPTION.queryIndex).emptyIfNull().internEmpty()
            numDiagramObjects = resultSet.getInt(table.NUM_DIAGRAM_OBJECTS.queryIndex)
        }

        return true
    }

    /**
     * Try and add the [identifiedObject] to the [service], and throw an [Exception] if unsuccessful.
     *
     * @param identifiedObject The [IdentifiedObject] to add to the [service].
     *
     * @return true in all instances, otherwise it throws.
     * @throws DuplicateMRIDException If the [IdentifiedObject.mRID] has already been used.
     * @throws UnsupportedIdentifiedObjectException If the [IdentifiedObject] is not supported by the [service]. This is an indication of an internal coding
     *   issue, rather than a problem with the data being read, and in a correctly configured system will never occur.
     */
    @Throws(DuplicateMRIDException::class, UnsupportedIdentifiedObjectException::class)
    protected fun BaseService.addOrThrow(identifiedObject: IdentifiedObject): Boolean {
        return if (tryAdd(identifiedObject)) {
            true
        } else {
            val duplicate = get<IdentifiedObject>(identifiedObject.mRID)
            throw DuplicateMRIDException(
                "Failed to load ${identifiedObject.typeNameAndMRID()}. Unable to add to service '$name': duplicate MRID (${duplicate?.typeNameAndMRID()})"
            )
        }
    }

    private fun BaseService.addOrThrow(nameType: NameType): Boolean {
        return if (addNameType(nameType))
            true
        else
            throw DuplicateNameTypeException("Failed to load NameType ${nameType.name}. Unable to add to service '$name': duplicate NameType")
    }

}
