package org.msh.pdex.ipermit.services;

import org.msh.pdex.dto.tables.Headers;

/**
 * To make possiblity pass create headers function as parameter
 * @author alexk
 *
 */
@FunctionalInterface
public interface CreateHeaders {
	Headers execute();
}
