/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/
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
			CompositeView( view, bounds.width/3 @ (bounds.height) ).resize_(4+if(i==0){0}{1})
		});

		udefView = UdefListView( composites[0] );
		umapDefView = UMapDefListView( composites[1] );
		immDefView = UEvNetModDefListView( composites[2] );
	}
}
