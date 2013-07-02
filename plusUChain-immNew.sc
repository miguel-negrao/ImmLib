+ MUChain {

	*immNew { |surface...args|
		var f = {
			var busses = ClusterArg( surface.size.collect(500 + _) );
			MUChain( *( args ++ [ [\pannerout, [\u_o_ar_0_bus, busses] ] ] ) )
		};
		^switch(ImmLib.mode)
		{\vbap} {
			f.()
		}
		{\previewHRTF}{
			f.()
		}
		{\previewStereo}{
			var points = ClusterArg( surface.points.collect{ |p| Point(p.asRealVector3D.x,p.asRealVector3D.y) } );
			MUChain( *( args ++ [ [\stereoOutput, [\point, points ] ] ] ) )
		}
	}

}
		