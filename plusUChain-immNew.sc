+ MUChain {

	*immNew { |surface...args|
		var busses = ClusterArg( surface.size.collect(500 + _) );
		^MUChain( *( args ++ [ [\pannerout, [\u_o_ar_0_bus, busses] ] ] ) )
	}
}
		