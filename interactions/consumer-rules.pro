-dontwarn com.splunk.android.instrumentation.recording.interactions.**

# PointerInputObserverInjectorModifier
-keepclassmembers class androidx.compose.ui.CombinedModifier {
    final androidx.compose.ui.Modifier outer;
}

-keepnames class androidx.compose.ui.node.BackwardsCompatNode

-keepclassmembers class androidx.compose.ui.Modifier$Node {
    androidx.compose.ui.node.NodeCoordinator coordinator;
}

-keepclassmembers class androidx.compose.ui.node.NodeCoordinator {
    androidx.compose.ui.node.LayoutNode layoutNode;
}

-keepclassmembers class androidx.compose.ui.node.LayoutNode {
    androidx.compose.ui.Modifier modifier;
    void setModifier(androidx.compose.ui.Modifier);
}

-keepnames class androidx.compose.ui.node.ModifierLocalConsumerEntity

-keepclassmembers class androidx.compose.ui.node.ModifierLocalConsumerEntity {
    androidx.compose.ui.node.ModifierLocalProviderEntity provider;
}

-keepclassmembers class androidx.compose.ui.node.ModifierLocalProviderEntity {
    androidx.compose.ui.node.LayoutNode layoutNode;
}

-keepclassmembers class androidx.compose.ui.node.LayoutNode {
    androidx.compose.ui.node.Owner owner;
}

# ElementPathFinder
-keepnames class androidx.fragment.app.Fragment