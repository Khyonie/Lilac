package coffee.khyonieheart.lilac.parser.analyzer;

public class ParserStep
{
	private String state;
	private int startPointer;
	private int endPointer;

	public ParserStep(
		String state, 
		int start, 
		int end
	) {
		this.state = state;
		this.startPointer = start;
		this.endPointer = end;
	}

	public String getState() 
	{
		return state;
	}

	public int getStartPointer() 
	{
		return startPointer;
	}

	public int getEndPointer() 
	{
		return endPointer;
	}
}
