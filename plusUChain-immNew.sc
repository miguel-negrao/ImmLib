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
			var points = ClusterArg( surface.pointsRV3D.collect{ |p| Point(p.x, p.y) } );
			MUChain( *( args ++ [ [\stereoOutput, [\point, points ] ] ] ) )
		}
	}

}
		