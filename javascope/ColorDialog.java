import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.Vector;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.io.*;


class ColorDialog extends ScopePositionDialog  {
    List colorList;
    TextField colorName;
    Choice color;
    Slider red, green, blue;
    Button ok, reset, cancel, add, erase;
    jScope main_scope;
    Label label;
    Canvas color_test;
    int red_i, green_i, blue_i;
    boolean changed = false;
    Vector color_set = new Vector();
    Vector color_set_clone;
    Color color_vector[];
    String color_name[];
    private boolean reversed = false;
    
        
    static class Item {
	String name;
	Color  color;
	
	Item(String n, Color c)
	{
	    name = new String(n);
	    color = c; 
	}
    }
    public static final Color[]  COLOR_SET = {Color.black, Color.blue, Color.cyan, Color.darkGray,
					     Color.gray, Color.green, Color.lightGray, 
					     Color.magenta, Color.orange, Color.pink, 
					     Color.red, Color.yellow};
	
    public static final String[] COLOR_NAME = {"Black", "Blue", "Cyan", "DarkGray",
					     "Gray", "Green", "LightGray", 
					     "Magenta", "Orange", "Pink", 
					     "Red", "Yellow"};

    ColorDialog(Frame dw, String title)
    {

        super(dw, title, true);
	    setResizable(false);
	    //super.setFont(new Font("Helvetica", Font.PLAIN, 10));    

	    main_scope = (jScope)dw;
	    
	    if(!main_scope.is_applet)
	        GetPropertiesValue();
	    
        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);        

	    c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.BOTH;

        c.gridwidth = GridBagConstraints.REMAINDER;
	    label = new Label("Color list customization");
        gridbag.setConstraints(label, c);
        add(label);

//	    Panel p0 = new Panel();
//      p0.setLayout(new FlowLayout(FlowLayout.LEFT));
				
	    c.gridwidth = GridBagConstraints.BOTH;
	    label = new Label("Name");
        gridbag.setConstraints(label, c);
        add(label);
    		
	    colorName = new TextField(15);
	    colorName.addKeyListener(this);			
        gridbag.setConstraints(colorName, c);
        add(colorName);

        if(GetNumColor() == 0)
            ColorSetItems(COLOR_NAME, COLOR_SET);
	    setColorVector();
        GetColorsName();
	    color = new Choice();
	    for(int i = 1; i < color_name.length; i++)
	        color.addItem(color_name[i]);
	        

	color.addItemListener(this);
	gridbag.setConstraints(color, c);
	add(color);	

	c.gridwidth = GridBagConstraints.REMAINDER;
	color_test = new Canvas();
//	color_test.setBounds(10,10,10,10);
	color_test.setBackground(Color.black);	      	      
        gridbag.setConstraints(color_test, c);
	add(color_test);
	

	c.gridwidth = 2;
	c.gridheight = 5;
	colorList = new List(12, false);
	colorList.addItemListener(this);
	colorList.addKeyListener(this);			
        gridbag.setConstraints(colorList, c);
        add(colorList);

	label = new Label("Red");
	c.gridheight = 1;
        gridbag.setConstraints(label, c);
        add(label);
				
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = 1;
	red  = new Slider(Slider.HORIZONTAL, Slider.POS_LABEL_UP, 0, 0, 255);
	red.addAdjustmentListener(this);
        gridbag.setConstraints(red, c);
        add(red);

	c.gridwidth = GridBagConstraints.BOTH;
	label = new Label("Green");
	c.gridheight = 1;
        gridbag.setConstraints(label, c);
        add(label);
				
	c.gridwidth = GridBagConstraints.REMAINDER;
	green = new Slider(Slider.HORIZONTAL, Slider.POS_LABEL_UP, 0, 0, 255);
	green.addAdjustmentListener(this);
        gridbag.setConstraints(green, c);
        add(green);

	c.gridwidth = GridBagConstraints.BOTH;
	label = new Label("Blue");
	c.gridheight = 1;
        gridbag.setConstraints(label, c);
        add(label);
		
	c.gridwidth = GridBagConstraints.REMAINDER;
	blue  = new Slider(Slider.HORIZONTAL, Slider.POS_LABEL_UP, 0, 0, 255);
	blue.addAdjustmentListener(this);
        gridbag.setConstraints(blue, c);
        add(blue);
	
	Panel p1 = new Panel();
        p1.setLayout(new FlowLayout(FlowLayout.CENTER));
    	
	ok = new Button("Ok");
	ok.addActionListener(this);	
        p1.add(ok);

    	add = new Button("Add");
	add.addActionListener(this);	
        p1.add(add);

	erase = new Button("Erase");
	erase.addActionListener(this);	
        p1.add(erase);

	reset = new Button("Reset");
	reset.addActionListener(this);	
        p1.add(reset);
				    		

	cancel = new Button("Cancel");
	cancel.addActionListener(this);	
        p1.add(cancel);

    	c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(p1, c);
	add(p1);
	
	 
     }
 
     
    private void GetPropertiesValue()
    {
       ResourceBundle rb = main_scope.rb;
       String prop;
       int i = 0, len;
       
       if(rb == null) return;
       try {
        
       //prop = rb.getString("jScope.reversed");
       //if(prop != null && ( prop.equals("true") || prop.equals("false")))
       //{
       //  prop_reversed = new Boolean(prop).booleanValue();
       //}
 
       while(true) {
           prop = rb.getString("jScope.item_color_"+i);
           String name = new String(prop.substring(0, len = prop.indexOf(",")));
		   Color cr = StringToColor(new String(prop.substring(len + 2, prop.length())));
		   insertItemAt(name, cr, i);
           i++;
       }

       
       } catch(MissingResourceException e){}
       
    }     
     
     
     
    private Vector copyColorItemsVector(Vector in)
    {
	Vector out = new Vector(in.size());
	for(int i = 0; i < in.size(); i++)
	    out.addElement(new Item(((Item)in.elementAt(i)).name, ((Item)in.elementAt(i)).color));
	return out;   
    }
    
    public void ShowColorDialog(Component f)
    {
	setColorItemToList();
//	color_set_clone = (Vector)color_set.clone();
	color_set_clone = copyColorItemsVector(color_set);
	pack();
	setPosition(f);
    	show();	
    }
         
    
    public void setReversed(boolean reversed)
    {
      if(this.reversed != reversed)
      {
        this.reversed = reversed;
        reversedColor(color_name, color_vector);
	    setColorVector();
        GetColorsName();
//        main_scope.RepaintAllWaves();
      }
    }

    private void reversedColor(String[] color_name, Color[] colors)
    {
	    for(int i = 0; i < color_name.length; i++)
	    {
	        if(reversed)
	        {
	            if(colors[i].equals(Color.black))
	            {
	                Item c_item = new Item("White", Color.white);
	                color_set.setElementAt(c_item, i);
	                break;
	            }
	        } else {
	            if(colors[i].equals(Color.white))
	            {
	                Item c_item = new Item("Black", Color.black);
	                color_set.setElementAt(c_item, i);
	                break;
	            }
	        }
	    }
	    main_scope.setup.sd.SetColorList(); 
    }
    
    public void ColorSetItems(String[] color_name, Color[] colors)
    {
	    for(int i = 0; i < color_name.length; i++)
	    {
	        if(reversed)
	        {
	            if(colors[i].equals(Color.black))
	            {
	                Item c_item = new Item("White", Color.white);
	                color_set.addElement(c_item);
	                continue;
	            }
	        } else {
	            if(colors[i].equals(Color.white))
	            {
	                Item c_item = new Item("Black", Color.black);
	                color_set.addElement(c_item);
	                continue;
	            }
	        }
	        
	        Item c_item = new Item(color_name[i], colors[i]);
	        color_set.addElement(c_item);
	    }
    }
        
    
    public  Color GetColorAt(int idx)
    {
	    if(idx >= 0 && idx < color_set.size())
	    {
	        Color color = ((Item)color_set.elementAt(idx)).color;
	        return color;
	    }
	    return null;
    }
    
    public  String GetNameAt(int idx)
    {
	if(idx >= 0 && idx < color_set.size())
	{
	    String name = ((Item)color_set.elementAt(idx)).name;
	    return name;
	}
	return null;
    }
    
    public int GetNumColor()
    {
	    return color_set.size();
    } 

    public String[] GetColorsName()
    {
	    color_name = null;
	
	    if(color_set.size() > 0)
	    {
	        color_name = new String[color_set.size()];
	        for (int i = 0; i < color_set.size(); i++)
	        {
		        color_name[i] = new String(((Item)color_set.elementAt(i)).name);
	        }
	    }
	    return color_name;
    }

    public void itemStateChanged(ItemEvent e)
    {
    	Object ob = e.getSource();
	
	if(ob == colorList)
	{
            int color_idx = colorList.getSelectedIndex();
	    Item c_item = (Item)color_set.elementAt(color_idx);
	    SetSliderToColor(c_item.color);
	    colorName.setText(c_item.name);    	  
	}
	
	if(ob == color)
	{
            int color_idx = color.getSelectedIndex();
	    colorName.setText(COLOR_NAME[color_idx]);
	    SetSliderToColor(COLOR_SET[color_idx]);
	    
	}
    }
    
    public void removeAllColorItems()
    {
	color_set.removeAllElements();
    }
    
    public void insertItemAt(String name, Color color, int idx)
    {
	    Item c_item = new Item(name, color);
	    color_set.insertElementAt(c_item, idx);		
    }
    
   private Color getColor()
   {
      return new Color(red.getValue(), green.getValue(), blue.getValue());
   }
   
   public  Color[] setColorVector()
   {
      color_vector = new Color[color_set.size()];
    
      for(int i = 0; i < color_set.size(); i++)
        color_vector[i] = ((Item)color_set.elementAt(i)).color;
      return color_vector;
   }
   
   public Color[] getColors()
   {
       return color_vector;
   }
   
   private void SetSliderToColor(Color c)
   {
	red.setValue(c.getRed());
	green.setValue(c.getGreen());
	blue.setValue(c.getBlue());
	color_test.setBackground(c);
	color_test.repaint();	      	      
   }
   
   private void AddUpdateItem(String name, Color color)
   {
        int i;
	    if(name == null || name.length() == 0)
	        return;
    	Item c_item = new Item(name, color);
	    String c_name[] = GetColorsName();
	        for(i = 0; c_name != null && i < c_name.length && !c_name[i].equals(name); i++);
	    if(c_name == null || i == c_name.length) {
	        color_set.addElement(c_item);
	        colorList.add(name);
	    }	    
	    else
	        color_set.setElementAt(c_item, i);	    	
   }
    
   public void keyPressed(KeyEvent e)
   {
      Object ob = e.getSource();
      char key  = e.getKeyChar();
        
     if(key == KeyEvent.CHAR_UNDEFINED)
	 return;		  	      	

      if(key == KeyEvent.VK_DELETE)
      {
	    if(ob == colorList) {
	        int idx = colorList.getSelectedIndex();
            colorList.remove(idx);
	        color_set.removeElementAt(idx);
	   	    colorName.setText("");
	    }
      }
      if(key == KeyEvent.VK_ENTER)
      { 
	    if(ob == colorName) {
	        AddUpdateItem(colorName.getText(), getColor());	    
	    }
      }	 	
   }
        
    public void setColorItemToList()
    {
	if(colorList.getItemCount() > 0)    
	    colorList.removeAll();
	for(int i = 0; i < color_set.size(); i++)
	{	    
	    colorList.add(((Item)color_set.elementAt(i)).name);
	}   
    }
    
    public void adjustmentValueChanged(AdjustmentEvent e)
    {
	    color_test.setBackground(getColor());
	    color_test.repaint();	      	      
    }
    
    private Color StringToColor(String str)
    {
	int pos;
	String tmp = str.substring(str.indexOf("=") + 1, pos = str.indexOf(","));
	int r = new Integer(tmp).intValue();
	tmp = str.substring(pos + 3, pos = str.indexOf(",", pos + 1));
	int g = new Integer(tmp).intValue();
	tmp = str.substring(pos + 3, str.indexOf("]", pos + 1));
	int b = new Integer(tmp).intValue();
	int c = (r<<16 | g << 8 | b);
	return(new Color(c));
    }

    
    public int fromFile(ReaderConfig in, String prompt) throws IOException
    {
    	String str;
	    int error = 0;
	    removeAllColorItems();

	    while((str = in.readLine()) != null) {

	        if(str.indexOf(prompt) != -1)
	        {
		        int len;
		        int i = new Integer(str.substring("Scope.item_color_".length(), len = str.indexOf(":"))).intValue();
		        String name = new String(str.substring(len  + 2, len = str.indexOf(",")));
		        Color cr = StringToColor(new String(str.substring(len + 2, str.length())));
		        insertItemAt(name, cr, i);
		        continue;
	        }
	    }
	    
	    if(GetNumColor() == 0)
	    {
	        if(main_scope.rb != null)
	        {
	            GetPropertiesValue();
	        } else {
                ColorSetItems(COLOR_NAME, COLOR_SET);
	            setColorVector();
                GetColorsName();
            }
	    }
	    return error;
    }
    
    
    public void toFile(PrintWriter out, String prompt)
    {
	for(int i = 0; i < GetNumColor(); i++)
        {
	    jScope.writeLine(out, prompt + i + ": " , GetNameAt(i) + "," + GetColorAt(i));		
        }
    }

 
    public void actionPerformed(ActionEvent e) {

	Object ob = e.getSource();
	int i;
    
	if (ob == ok)
	{
	    if(ob == ok) {
		    color_set_clone = null;
    		setVisible(false);
	    }
	    AddUpdateItem(colorName.getText(), getColor());	    	
	    setColorVector();
	    main_scope.setup.sd.SetColorList(); 
	    main_scope.RepaintAllWaves();
    }
	
	if(ob == add)
	{
	    AddUpdateItem(colorName.getText(), getColor());	    	
	}

	if(ob == erase)
	{ 
	    colorName.setText("");
	    removeAllColorItems();
	    if(colorList.getItemCount() > 0)    
		    colorList.removeAll();
		AddUpdateItem(COLOR_NAME[0], COLOR_SET[0]);
	    setColorVector();
	}
		
	if (ob == reset)
	{
	    color_set = copyColorItemsVector(color_set_clone);
	    setColorItemToList();
	    setColorVector();
	}  

	if (ob == cancel)
	{
	    color_set = copyColorItemsVector(color_set_clone);
	    setColorItemToList();
	    setColorVector();
	    color_set_clone = null;
	    setVisible(false);
	}
	//main_scope.validate();
    }
}
