package coffee.khyonieheart.lilac.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentData
{
	private List<String> leadingComments = new ArrayList<>();
	private String inlineComment = null;

	public String getInlineComment()
	{
		return inlineComment;
	}

	public List<String> getLeadingComments()
	{
		return Collections.unmodifiableList(leadingComments);
	}
}
