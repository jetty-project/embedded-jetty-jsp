package org.eclipse.jetty.demo.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class Leaving extends TagSupport
{
    public int doStartTag() throws JspException
    {
        try
        {
            pageContext.getOut().write("Leaving scripting behind");
        }
        catch(Exception e)
        {
            throw new JspException(e.getMessage());
        }
        return EVAL_PAGE;
    }
}
