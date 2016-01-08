
package de.fhg.fokus.ims.core.sdp;


import java.util.Vector;


public class Iterator
{
   Vector v;
   int i;

   public Iterator(Vector vector)
   {  v=vector;
      i=-1;
   }  

   public boolean hasNext()
   {  return i<(v.size()-1);
   }

   public Object next()
   {  if (++i<v.size()) return v.elementAt(i);
      else return null;
   }

   public void remove()
   {  v.removeElementAt(i);
      i--;
   }
}

