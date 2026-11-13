package coffee.khyonieheart.lilac.util;

/**
 * A fixed-length stack which uses int primitives to avoid autoboxing.
 */
public class IntStack
{
	private int[] stack;
	private int pointer = 0;

	public IntStack(int size)
	{
		this.stack = new int[size];
	}

	public void push(int value)
	{
		this.stack[pointer++] = value;
	}

	public int pop()
	{
		return this.stack[--pointer];
	}

	public int peek()
	{
		return this.stack[pointer - 1];
	}

	public int size()
	{
		return this.pointer;
	}

	public boolean isEmpty()
	{
		return this.pointer == 0;
	}
}
