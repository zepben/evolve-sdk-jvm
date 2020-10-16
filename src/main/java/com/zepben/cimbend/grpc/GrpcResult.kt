/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 * This file is part of evolve-sdk-jvm.
 *
 * evolve-sdk-jvm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * evolve-sdk-jvm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with evolve-sdk-jvm.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.zepben.cimbend.grpc

/**
 * The result of a gRPC call.
 * @property wasSuccessful Indicates if the call was resolved without error
 * @property result The result of the call if [wasSuccessful] is true, otherwise null. The result may still be null even if successful.
 * @property thrown The exception that was caught if [wasSuccessful] is false, otherwise null.
 */
data class GrpcResult<T>(
    val wasSuccessful: Boolean,
    val result: T?,
    val thrown: Throwable?
) {

    companion object {

        @JvmStatic
        fun <T> of(result: T?): GrpcResult<T> {
            return GrpcResult(true, result, null)
        }

        @JvmStatic
        fun <T> ofError(thrown: Throwable): GrpcResult<T> {
            return GrpcResult(false, null, thrown)
        }

    }

}
