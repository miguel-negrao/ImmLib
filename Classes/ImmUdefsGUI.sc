ImmUdefsGUI {

	classvar <>current;

	var <view, <composites, <udefView, <umapDefView, <immDefView;

	*new { |parent, bounds, makeCurrent = true|
		if( parent.isNil && { current.notNil && { current.view.isClosed.not } } ) {
			^current.front;
		} {
			^super.new.init( parent, bounds ).makeCurrent( makeCurrent );
		};
	}

	makeCurrent { |bool| if( bool == true ) { current = this } }

	front { view.findWindow.front }

	init { |parent, bounds|
		if( parent.notNil ) {
			bounds = bounds ?? { parent.bounds.moveTo(0,0).insetBy(4,4) };
		} {
			bounds = bounds ? Rect(
				Window.screenBounds.width - 505,
				Window.screenBounds.height - 750,
				600, 600
			);
		};

		view = EZCompositeView( parent ? "Udefs", bounds, true, 0@0, 0@0 ).resize_(5);
		bounds = view.bounds;
		view.onClose_({
			if( current == this ) { current = nil };
		});

		composites = 3.collect({ |i|
			 CompositeView( view, bounds.width/3 @ (bounds.height) ).resize_(4+i)
		});

		udefView = UdefListView( composites[0] );
		umapDefView = UMapDefListView( composites[1] );
		immDefView = UEvNetModDefListView( composites[2] );
	}
}