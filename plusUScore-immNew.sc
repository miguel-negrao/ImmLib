+ UScore {

	*immNew { |surface ... events|
		var m = surface.points.size;
		var busses = ClusterArg( m.collect(500 + _) );
		var ugroups = ClusterArg( m.collect({ |i| ("immGroup"++i).asSymbol }) );
		var panners;

		events.do( _.ugroup_( ugroups ) );

		panners = MUChain([\vbap3D_Simple_Panner,
			[\angles, surface.pointsDegrees, \spread, 0.0, \u_i_ar_0_bus, busses ]
		]).ugroup_(ugroups).addAction_('addToTail').hideInGUI_(true);

		^UScore(*(events++panners) ).cleanOverlaps
	}
}
		