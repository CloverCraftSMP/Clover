package com.clovercraftsmp.clover.companion;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LuckPermsSync {
    static final String META_KEY = "discord-role-id";

    public static void apply(UUID uuid, long[] roleIds) {
        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().getUser(uuid);
        if (user == null) return;
        Set<String> roles = Arrays.stream(roleIds).mapToObj(Long::toString).collect(Collectors.toSet());
        for (Group group : lp.getGroupManager().getLoadedGroups()) {
            String id = group.getCachedData().getMetaData().getMetaValue(META_KEY);
            if (id != null && roles.contains(id)) {
                user.transientData().add(InheritanceNode.builder(group).build());
            }
        }
    }
}
