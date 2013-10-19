package hudson.plugins.ec2.win.winrm.soap;

import org.dom4j.Element;

public class Utilities
{
  static Element mustUnderstand(Element e)
  {
    return e.addAttribute("mustUnderstand", "true");
  }

  static Element mustNotUnderstand(Element e)
  {
    return e.addAttribute("mustUnderstand", "false");
  }

}
