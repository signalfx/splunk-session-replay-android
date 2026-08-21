-dontwarn com.splunk.android.instrumentation.recording.wireframe.**

# Classes used in code
-dontwarn com.google.android.material.appbar.AppBarLayout
-dontwarn com.google.android.material.appbar.CollapsingToolbarLayout
-dontwarn com.google.android.material.internal.CollapsingTextHelper
-dontwarn com.google.android.material.tabs.TabLayout
-dontwarn com.google.android.material.tabs.TabLayout$Tab
-dontwarn com.google.android.material.tabs.TabLayout$TabView
-dontwarn com.google.android.material.textfield.TextInputLayout

# TextInputLayoutDescriptor
-keepclassmembers class com.google.android.material.textfield.TextInputLayout {
    final com.google.android.material.internal.CollapsingTextHelper collapsingTextHelper;
}

# TabLayoutTabViewDescriptor
-keepnames class com.google.android.material.tabs.TabLayout

-keepclassmembers class com.google.android.material.tabs.TabLayout$TabView {
    private android.graphics.drawable.Drawable baseBackgroundDrawable;
}

# RecyclerViewDescriptor
-keepnames class androidx.recyclerview.widget.RecyclerView

-keepclassmembers class androidx.recyclerview.widget.RecyclerView {
    final java.util.ArrayList mItemDecorations;
    final androidx.recyclerview.widget.RecyclerView$State mState;
}

# DrawerLayoutDescriptor
-keepnames class androidx.drawerlayout.widget.DrawerLayout

-keepclassmembers class androidx.drawerlayout.widget.DrawerLayout {
    private float mScrimOpacity;
    private int mScrimColor;
}

# AppBarLayoutDescriptor
-keepnames class com.google.android.material.appbar.AppBarLayout

-keepclassmembers class com.google.android.material.appbar.AppBarLayout {
    private int currentOffset;
}

# CollapsingToolbarLayoutDescriptor
-keepnames class com.google.android.material.appbar.CollapsingToolbarLayout

-keepclassmembers class com.google.android.material.appbar.CollapsingToolbarLayout {
    private boolean isToolbarChild(android.view.View);
    private int scrimAlpha;
    private androidx.appcompat.widget.Toolbar toolbar;
    private android.view.ViewGroup toolbar;
    final com.google.android.material.internal.CollapsingTextHelper collapsingTextHelper;
    private boolean drawCollapsingTitle;
    android.graphics.drawable.Drawable statusBarScrim;
    androidx.core.view.WindowInsetsCompat lastInsets; #v1.2.0
    private int tabSelectedIndicatorColor; #v1.3.0
}

-keepclassmembers class com.google.android.material.internal.CollapsingTextHelper {
    public void draw(android.graphics.Canvas);
    private android.text.StaticLayout textLayout;
    private float currentDrawX;
    private float currentDrawY;
}

# TabLayoutSlidingTabIndicatorDescriptor
-keepclassmembers class com.google.android.material.tabs.TabLayout$SlidingTabIndicator {
    private final android.graphics.Paint selectedIndicatorPaint;
    int indicatorLeft;
    int indicatorRight;
}

-keepclassmembers class com.google.android.material.tabs.TabLayout {
    private int tabSelectedIndicatorColor; #v1.3.0+
}

# Declared Canvas override method
-keepclassmembers class com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas {
    public int save(int);
}

-keepclassmembers class com.splunk.android.instrumentation.recording.wireframe.canvas.LoggingCanvas {
    public int save(int);
}

-keepclassmembers class com.splunk.android.instrumentation.recording.wireframe.canvas.LoggingSkeletonCanvas {
    public int save(int);
}

# SessionReplayDrawModifier
-keepclassmembers class androidx.compose.ui.node.LayoutNodeDrawScope {
    private final androidx.compose.ui.graphics.drawscope.CanvasDrawScope canvasDrawScope;
    private androidx.compose.ui.node.DrawEntity drawEntity;
    private androidx.compose.ui.node.DrawModifierNode drawNode;
}

-keepclassmembers class androidx.compose.ui.graphics.drawscope.CanvasDrawScope {
    private final androidx.compose.ui.graphics.drawscope.CanvasDrawScope$DrawParams drawParams;
}

-keepclassmembers class androidx.compose.ui.graphics.drawscope.CanvasDrawScope$DrawParams {
    private androidx.compose.ui.graphics.Canvas canvas;
}

-keepclassmembers class androidx.compose.ui.graphics.AndroidCanvas {
    private android.graphics.Canvas internalCanvas;
}

-keepclassmembers class androidx.compose.ui.node.LayoutNodeEntity {
    private androidx.compose.ui.node.LayoutNodeEntity next;
    private final androidx.compose.ui.Modifier modifier;
    final androidx.compose.ui.node.LayoutNodeWrapper layoutNodeWrapper;
}

-keepclassmembers class androidx.compose.ui.node.LayoutNodeWrapper {
    final androidx.compose.ui.node.LayoutNode layoutNode;
}

-keepclassmembers class androidx.compose.ui.draw.DrawBackgroundModifier {
    kotlin.jvm.functions.Function1 onDraw;
}

-keepnames class androidx.compose.ui.node.DelegatableNode

-keepnames class androidx.compose.ui.focus.FocusOwner

-keepnames class androidx.compose.ui.node.LayoutNodeDrawScopeKt

-keepclassmembers class androidx.compose.ui.node.LayoutNodeDrawScopeKt {
    androidx.compose.ui.node.DrawModifierNode nextDrawNode(androidx.compose.ui.node.DelegatableNode);
    androidx.compose.ui.Modifier$Node nextDrawNode(androidx.compose.ui.node.DelegatableNode); #v1.7.x
}

-keepclassmembers class androidx.compose.ui.node.BackwardsCompatNode {
   private androidx.compose.ui.Modifier$Element element;
}

-keepclassmembers class androidx.compose.ui.draw.DrawBackgroundModifier {
   kotlin.jvm.functions.Function1 onDraw;
}

-keepnames class androidx.compose.ui.viewinterop.AndroidViewHolder

-keepnames class androidx.compose.ui.viewinterop.AndroidViewHolder$* { *; }

# ViewPath
-keepnames class androidx.viewpager2.widget.ViewPager2$RecyclerViewImpl

-keepnames class androidx.viewpager2.widget.ViewPager2

-keepclassmembers class androidx.viewpager2.widget.ViewPager2 {
    private androidx.recyclerview.widget.LinearLayoutManager mLayoutManager;
}

-keepnames class androidx.viewpager.widget.ViewPager

-keepclassmembers class androidx.viewpager.widget.ViewPager {
    private final java.util.ArrayList mItems;
}

-keepclassmembers class androidx.viewpager.widget.ViewPager$ItemInfo {
    java.lang.Object object;
    int position;
}

# AndroidComposeViewDescriptor
-keepclassmembers class androidx.compose.ui.platform.RenderNodeLayer {
    private boolean isDirty;
}

-keepnames class androidx.compose.ui.platform.AndroidComposeView

-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    private final java.util.List dirtyLayers;
    private final androidx.compose.ui.node.LayoutNode root;
}

-keepclassmembers class androidx.compose.ui.node.LayoutNode {
    private final androidx.compose.runtime.collection.MutableVector _foldedChildren; #v1.2.1
    private final androidx.compose.ui.node.MutableVectorWithMutationTracking _foldedChildren; #v1.3.1
    androidx.compose.ui.layout.MeasurePolicy measurePolicy;
    androidx.compose.ui.Modifier modifier;
    androidx.compose.ui.Modifier _modifier; #v1.7.x
    androidx.compose.ui.viewinterop.AndroidViewHolder interopViewFactoryHolder; #v1.4.x
    androidx.compose.ui.node.NodeChain nodes;
    java.util.List getChildren$ui_release();
}

# ComposeTextFieldSensitivity
-keepclassmembers class androidx.compose.ui.node.LayoutNode {
    androidx.compose.ui.node.Owner owner;
    boolean isDeactivated; #v1.6+
    void setModifier(androidx.compose.ui.Modifier);
}

-keepclassmembers class androidx.compose.ui.CombinedModifier {
    final androidx.compose.ui.Modifier outer;
    final androidx.compose.ui.Modifier inner;
}

-keepnames class androidx.compose.foundation.text.VerticalScrollLayoutModifier

-keepnames class androidx.compose.foundation.text.HorizontalScrollLayoutModifier

-keepnames class androidx.compose.foundation.text.input.internal.TextFieldCoreModifier #v1.7+

-keepnames class androidx.compose.foundation.text.input.internal.TextFieldDecoratorModifier #v1.7+

-keepnames class androidx.compose.foundation.text.input.internal.CoreTextFieldSemanticsModifier #v1.8+

-keepnames class androidx.compose.ui.semantics.AppendedSemanticsElement #v1.5+

-keepclassmembers class androidx.compose.ui.semantics.AppendedSemanticsElement {
    boolean mergeDescendants;
}

-keepnames class androidx.compose.ui.semantics.SemanticsModifierCore #v1.2 - v1.4

-keepclassmembers class androidx.compose.ui.node.NodeChain {
    androidx.compose.ui.Modifier$Node head;
}

-keepclassmembers class androidx.compose.foundation.AndroidEdgeEffectOverscrollEffect {
    private final android.widget.EdgeEffect topEffect;
    private final android.widget.EdgeEffect bottomEffect;
    private final android.widget.EdgeEffect leftEffect;
    private final android.widget.EdgeEffect rightEffect;
}

-keepclassmembers class androidx.compose.ui.node.MutableVectorWithMutationTracking {
    final androidx.compose.runtime.collection.MutableVector vector;
}

-keepnames class androidx.compose.ui.layout.LayoutNodeSubcompositionsState$createMeasurePolicy$* { *; }

-keepnames class androidx.compose.foundation.lazy.layout.LazyLayoutKt$LazyLayout$* { *; }

-keepnames class androidx.compose.foundation.lazy.LazyListKt$rememberLazyListMeasurePolicy$* { *; }

-keepnames class androidx.compose.foundation.AndroidEdgeEffectOverscrollEffect

-keepnames class androidx.compose.foundation.DrawOverscrollModifier

-keepclassmembers class androidx.compose.foundation.DrawOverscrollModifier {
    androidx.compose.foundation.AndroidEdgeEffectOverscrollEffect overscrollEffect;
}

-keepnames class androidx.collection.MutableObjectList {
    boolean none();
}

-keepnames class androidx.compose.foundation.ScrollableAreaElement

-keepclassmembers class androidx.compose.foundation.ScrollableAreaElement {
    androidx.compose.foundation.OverscrollEffect overscrollEffect;
}

-keepnames class androidx.compose.foundation.AndroidEdgeEffectOverscrollEffect

-keepclassmembers class androidx.compose.foundation.AndroidEdgeEffectOverscrollEffect {
    androidx.compose.foundation.EdgeEffectWrapper edgeEffectWrapper;
}

-keepclassmembers class androidx.compose.foundation.ScrollingContainerElement {
    androidx.compose.foundation.OverscrollEffect overscrollEffect;
}

-keepclassmembers class androidx.compose.foundation.EdgeEffectWrapper {
    android.widget.EdgeEffect topEffect;
    android.widget.EdgeEffect bottomEffect;
    android.widget.EdgeEffect leftEffect;
    android.widget.EdgeEffect rightEffect;
    android.widget.EdgeEffect topEffectNegation;
    android.widget.EdgeEffect bottomEffectNegation;
    android.widget.EdgeEffect leftEffectNegation;
    android.widget.EdgeEffect rightEffectNegation;

    android.widget.EdgeEffect getOrCreateTopEffect();
    android.widget.EdgeEffect getOrCreateBottomEffect();
    android.widget.EdgeEffect getOrCreateLeftEffect();
    android.widget.EdgeEffect getOrCreateRightEffect();
    android.widget.EdgeEffect getOrCreateTopEffectNegation();
    android.widget.EdgeEffect getOrCreateBottomEffectNegation();
    android.widget.EdgeEffect getOrCreateLeftEffectNegation();
    android.widget.EdgeEffect getOrCreateRightEffectNegation();
}

-keepnames class androidx.compose.animation.SharedBoundsNode

-keepclassmembers class androidx.compose.ui.Modifier$Node {
    androidx.compose.ui.node.NodeCoordinator coordinator;
    androidx.compose.ui.Modifier$Node child;
}

-keepclassmembers class androidx.compose.ui.node.NodeCoordinator {
    androidx.compose.ui.node.NodeCoordinator wrapped;
    androidx.compose.ui.node.NodeCoordinator wrappedBy;
}

# ComposeInfo

-keepnames class androidx.compose.ui.node.UnplacedAwareModifierNode

-keepnames class androidx.compose.ui.node.SortedSet

-keepnames class androidx.compose.ui.node.DistanceAndFlags

-keepnames class androidx.compose.ui.graphics.layer.GraphicsLayer

-keepnames class androidx.compose.ui.platform.PlatformTextInputModifierNode

-keepnames class androidx.compose.ui.node.CompositionLocalConsumerModifierNode

-keepnames class androidx.compose.ui.focus.FocusOwner

-keepnames class androidx.compose.ui.node.DelegatableNode

-keepnames class androidx.compose.ui.text.PlatformTextStyle
