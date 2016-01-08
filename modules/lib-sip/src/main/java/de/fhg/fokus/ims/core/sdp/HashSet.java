package de.fhg.fokus.ims.core.sdp;


import java.util.Vector;

public class HashSet
{
   Vector set;

   public HashSet()
   {  set=new Vector();
   }  

   public int size()
   {  return set.size();
   }

   public boolean isEmpty()
   {  return set.isEmpty();
   }

   public boolean add(Object o)
   {  set.addElement(o);
      return true;
   }

   public boolean remove(Object o)
   {  return set.removeElement(o);
   }

   public boolean contains(Object o)
   {  return set.contains(o);
   }
   
   public Iterator iterator()
   {  return new Iterator(set);
   }   
}

