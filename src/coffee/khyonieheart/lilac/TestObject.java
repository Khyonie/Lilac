package coffee.khyonieheart.lilac;

import java.util.List;
import java.util.Map;

import coffee.khyonieheart.lilac.api.LilacExpose;

public class TestObject
{
	@LilacExpose private byte a_byte = 0b0000_0000;
	@LilacExpose private short a_short = (short) 0xFFFF;
	@LilacExpose private int an_int = 100;
	@LilacExpose private long a_long = Long.MAX_VALUE;
	@LilacExpose private boolean a_boolean = true;
	@LilacExpose private String a_string = "A string";
	// @LilacExpose private int[] an_array = new int[] { 0, 2, 4, 8, 16, 32 };
	@LilacExpose private List<String> a_list = List.of("A", "list", "of", "strings");
	@LilacExpose private Map<Integer, String> a_map = Map.of(0, "A", 1, "map", 2, "of", 3, "strings");
}
