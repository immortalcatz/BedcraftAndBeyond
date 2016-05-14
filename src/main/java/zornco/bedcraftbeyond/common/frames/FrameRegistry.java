package zornco.bedcraftbeyond.common.frames;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.*;

public class FrameRegistry {

   // Boolean is used to force only a set of metadata to be allowed
   private HashMap<ResourceLocation, Set<Integer>> woodFrames;
   private HashMap<ResourceLocation, Set<Integer>> stoneFrames;

   public enum EnumBedFrameType { WOOD, STONE }

   private static FrameRegistry INSTANCE;

   private FrameRegistry(){
      woodFrames = new HashMap<>();
      stoneFrames = new HashMap<>();
   }

   public static FrameRegistry getInstance(){
      if(INSTANCE == null) INSTANCE = new FrameRegistry();
      return INSTANCE;
   }

   public static void dumpFrameList(){
      // This makes me sad.
      getInstance().woodFrames.clear();
      getInstance().stoneFrames.clear();
   }

   private static HashMap<ResourceLocation, Set<Integer>> getFrameSet(EnumBedFrameType type){
      HashMap<ResourceLocation, Set<Integer>> set = null;
      switch(type){
         case WOOD:
            set = getInstance().woodFrames; break;
         case STONE:
            set = getInstance().stoneFrames; break;
      }

      return set;
   }

   public static boolean addToFrameList(EnumBedFrameType type, ResourceLocation registryName){
      HashMap<ResourceLocation, Set<Integer>> set = getFrameSet(type);
      if(!set.containsKey(registryName)){
         set.put(registryName, new HashSet<Integer>());
         return true;
      }

      return false;
   }

   public static boolean addToFrameList(EnumBedFrameType type, ResourceLocation registryName, int meta){
      HashMap<ResourceLocation, Set<Integer>> set = getFrameSet(type);
      boolean added = false;
      if(set.containsKey(registryName))
         added = set.get(registryName).add(meta);
      else {
         HashSet<Integer> newSet = new HashSet<>();
         newSet.add(meta);
         set.put(registryName, newSet);
         added = true;
      }

      return added;
   }

   public static boolean removeFromFrameList(EnumBedFrameType type, ResourceLocation registryName){
      HashMap<ResourceLocation, Set<Integer>> set = null;
      switch(type){
         case WOOD:
            set = getInstance().woodFrames; break;
         case STONE:
            set = getInstance().stoneFrames; break;
      }

      if(set.containsKey(registryName)){
         set.remove(registryName);
         return true;
      }

      return false;
   }

   public static long getFrameTypeCount(EnumBedFrameType type) {
      long amt = 0;
      HashMap<ResourceLocation, Set<Integer>> set = getFrameSet(type);
      for(ResourceLocation rl : set.keySet()){
         if(set.get(rl).size() == 0){
            // TODO: Possibly find a better means to add up subItems on a server
            ++amt; continue;
         }

         amt += set.get(rl).size();
      }

      return amt;
   }

   public static Set<ResourceLocation> getWoodFrameSet(){
      Set<ResourceLocation> allFrames = new HashSet<>();
      for(Map.Entry<ResourceLocation, Set<Integer>> woodEntry : getInstance().woodFrames.entrySet()){
         if(woodEntry.getValue().size() == 0) {
            allFrames.add(woodEntry.getKey());
            continue;
         }

         for(int i : woodEntry.getValue()) allFrames.add(new ResourceLocation(woodEntry.getKey() + "@" + i));
      }

      return allFrames;
   }
}