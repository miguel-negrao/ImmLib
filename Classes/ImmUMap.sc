ImmUMap : UMap {
	var <surface;

	*new { |surface, def, args, mod|
		var us = surface.points.flop[0];
		var vs = surface.points.flop[1];
		var args1 = args ? [];
		var args2 = args1 ++[\u, ClusterArg(us), \v, ClusterArg(vs)];

		^super.new.init( def, args2, mod ).initImmUMap(surface)
	}

	initImmUMap { |surf|
		surface = surf
	}

	storeArgs {
		var initArgs, initDef;
		initArgs = this.getInitArgs;
		initDef = if( this.def.class.callByName ) {
		    this.defName
		} {
		    this.def
		};
		if( mod.notNil ) {
			^[ surface, initDef, initArgs, mod ];
		} {
			if( (initArgs.size > 0) ) {
				^[ surface, initDef, initArgs ];
			} {
				^[ surface, initDef ];
			};
		};
	}

	getInitArgs {
		var defArgs;
		defArgs = (this.def.args( this ) ? []).clump(2);
		^args.clump(2).select({ |item, i|
			(item != defArgs[i]) &&
			{ this.dontStoreArgNames.includes( item[0] ).not } and:
			{ [\u,\v].includes(item[0]).not };
		 }).collect({ |item|
			 var umapArgs;
			 if( item[1].isUMap ) {
				 umapArgs = item[1].storeArgs;
				 if( umapArgs.size == 1 ) {
				 	[ item[0], umapArgs[0] ]
				 } {
					 [ item[0], umapArgs ]
				 };
			 } {
				 item
			 };
		 }).flatten(1);
	}

}