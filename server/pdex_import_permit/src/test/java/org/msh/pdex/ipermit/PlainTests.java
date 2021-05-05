package org.msh.pdex.ipermit;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Test;
import org.msh.pdex.dto.tables.TableCell;


/**
 * Plain tests without Spring Boot framework
 * @author alexk
 *
 */
public class PlainTests {
	@Test
	public void shouldFormatBigDecimalCell() {
		BigDecimal bd = new BigDecimal(122333455.12);
		TableCell cell = TableCell.instanceOf("1", bd, Locale.US);
		System.out.println(cell.getValue());
		
	}
}
