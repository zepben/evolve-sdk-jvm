/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.diagram

import com.zepben.evolve.cim.iec61970.base.diagramlayout.*
import com.zepben.evolve.database.sqlite.cim.CimReader
import com.zepben.evolve.database.sqlite.extensions.getNullableString
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.diagramlayout.TableDiagramObjectPoints
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.diagramlayout.TableDiagramObjects
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.diagramlayout.TableDiagrams
import com.zepben.evolve.services.common.extensions.ensureGet
import com.zepben.evolve.services.common.extensions.getOrThrow
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.diagram.DiagramService
import java.sql.ResultSet
import java.sql.SQLException

/**
 * A class for reading the [DiagramService] tables from the database.
 *
 * @property service The [DiagramService] to populate from the database.
 */
class DiagramCimReader(
    override val service: DiagramService
) : CimReader(service) {

    // ###########################
    // # IEC61970 Diagram Layout #
    // ###########################

    /**
     * Create a [Diagram] and populate its fields from [TableDiagrams].
     *
     * @param table The database table to read the [Diagram] fields from.
     * @param resultSet The record in the database table containing the fields for this [Diagram].
     * @param setIdentifier A callback to register the mRID of this [Diagram] for logging purposes.
     *
     * @return true if the [Diagram] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableDiagrams, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val diagram = Diagram(setIdentifier(resultSet.getString(table.MRID.queryIndex))).apply {
            diagramStyle = DiagramStyle.valueOf(resultSet.getString(table.DIAGRAM_STYLE.queryIndex))
            orientationKind = OrientationKind.valueOf(resultSet.getString(table.ORIENTATION_KIND.queryIndex))
        }

        return loadIdentifiedObject(diagram, table, resultSet) && service.addOrThrow(diagram)
    }

    /**
     * Create a [DiagramObject] and populate its fields from [TableDiagramObjects].
     *
     * @param table The database table to read the [DiagramObject] fields from.
     * @param resultSet The record in the database table containing the fields for this [DiagramObject].
     * @param setIdentifier A callback to register the mRID of this [DiagramObject] for logging purposes.
     *
     * @return true if the [DiagramObject] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableDiagramObjects, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val diagramObject = DiagramObject(setIdentifier(resultSet.getString(table.MRID.queryIndex))).apply {
            diagram = service.ensureGet(resultSet.getString(table.DIAGRAM_MRID.queryIndex), typeNameAndMRID())
            diagram?.addDiagramObject(this)

            identifiedObjectMRID = resultSet.getNullableString(table.IDENTIFIED_OBJECT_MRID.queryIndex)
            style = resultSet.getNullableString(table.STYLE.queryIndex)
            rotation = resultSet.getDouble(table.ROTATION.queryIndex)
        }

        return loadIdentifiedObject(diagramObject, table, resultSet) && service.addOrThrow(diagramObject)
    }

    /**
     * Create a [DiagramObjectPoint] and populate its fields from [TableDiagramObjectPoints].
     *
     * @param table The database table to read the [DiagramObjectPoint] fields from.
     * @param resultSet The record in the database table containing the fields for this [DiagramObjectPoint].
     * @param setIdentifier A callback to register the mRID of this [DiagramObjectPoint] for logging purposes.
     *
     * @return true if the [DiagramObjectPoint] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableDiagramObjectPoints, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val diagramObjectMRID = setIdentifier(resultSet.getString(table.DIAGRAM_OBJECT_MRID.queryIndex))
        val sequenceNumber = resultSet.getInt(table.SEQUENCE_NUMBER.queryIndex)

        setIdentifier("$diagramObjectMRID-point$sequenceNumber")

        service.getOrThrow<DiagramObject>(diagramObjectMRID, "DiagramObjectPoint $sequenceNumber")
            .addPoint(
                DiagramObjectPoint(
                    resultSet.getDouble(table.X_POSITION.queryIndex),
                    resultSet.getDouble(table.Y_POSITION.queryIndex)
                ),
                sequenceNumber
            )

        return true
    }

}
