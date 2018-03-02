package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import com.therandomlabs.utils.collection.ArrayUtils;
import com.therandomlabs.utils.collection.TRLList;

public class GroupInfo implements Cloneable, Serializable {
	private static final long serialVersionUID = -571637128949272670L;

	public String primary;
	public String[] alternatives;

	public GroupInfo(String primary, String[] alternatives) {
		this.primary = primary;
		this.alternatives = alternatives;
	}

	@Override
	public GroupInfo clone() {
		return new GroupInfo(primary, alternatives);
	}

	public TRLList<String> getOtherGroupNames(String toExclude) {
		final TRLList<String> names = new TRLList<>(alternatives.length);

		if(!primary.equals(toExclude)) {
			names.add(primary);
		}

		for(String alternative : alternatives) {
			if(!alternative.equals(toExclude)) {
				names.add(alternative);
			}
		}

		return names;
	}

	public static GroupInfo getGroup(Collection<GroupInfo> groups, String groupName) {
		return getGroup(groups.toArray(new GroupInfo[0]), groupName);
	}

	public static GroupInfo getGroup(GroupInfo[] groups, String groupName) {
		for(GroupInfo group : groups) {
			if(group.primary.equals(groupName) ||
					ArrayUtils.contains(group.alternatives, groupName)) {
				return group;
			}
		}
		return null;
	}

	public static Map.Entry<String, GroupInfo> getGroup(Map<String, GroupInfo> groups,
			String groupName) {
		for(Map.Entry<String, GroupInfo> group : groups.entrySet()) {
			if(group.getValue().primary.equals(groupName) ||
					ArrayUtils.contains(group.getValue().alternatives, groupName)) {
				return group;
			}
		}
		return null;
	}

	public static boolean hasGroup(Collection<GroupInfo> groups, String groupName) {
		return hasGroup(groups.toArray(new GroupInfo[0]), groupName);
	}

	public static boolean hasGroup(GroupInfo[] groups, String groupName) {
		return getGroup(groups, groupName) != null;
	}

	public static boolean isPrimary(Collection<GroupInfo> groups, String groupName) {
		return isPrimary(groups.toArray(new GroupInfo[0]), groupName);
	}

	public static boolean isPrimary(GroupInfo[] groups, String groupName) {
		for(GroupInfo group : groups) {
			if(group.primary.equals(groupName)) {
				return true;
			}
		}
		return false;
	}
}
