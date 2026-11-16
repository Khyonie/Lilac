package coffee.khyonieheart.lilac.timing;

public class StageTimer
{
	private long start;
	private long afterScan, afterFilter, afterInterpretation;

	public void setStart()
	{
		this.start = System.currentTimeMillis();
	}

	public void setAfterScan()
	{
		this.afterScan = System.currentTimeMillis();
	}

	public void setAfterFilter()
	{
		this.afterFilter = System.currentTimeMillis();
	}

	public void setAfterInterpretation()
	{
		this.afterInterpretation = System.currentTimeMillis();
	}

	public long getScanTime()
	{
		return this.afterScan - this.start;
	}

	public long getFilterTime()
	{
		return this.afterFilter - this.afterScan;
	}

	public long getInterpretTime()
	{
		return this.afterInterpretation - this.afterFilter;
	}

	public long getTotalTime()
	{
		return this.afterInterpretation - this.start;
	}
}
