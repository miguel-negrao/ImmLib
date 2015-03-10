ImmU : ParU {
	var <>surface;
	asImmUnit{ ^this }
	*new { |surface, def, args, mod|
		^super.prNewBasic().initImmU2(surface, def, args, mod)
	}

	initImmU2 { |asurface, in, inArgs, inMod|
		surface = asurface; //only thing different
		n = asurface.size; //only thing different
		if( in.isKindOf( this.class.defClass ) ) {
			def = in;
			defName = in.name;
			if( defName.notNil && { defName.asUdef( this.class.defClass ) == def } ) {
				def = nil;
			};
		} {
			defName = in.asSymbol;
			def = nil;
		};
		if( this.def.notNil ) {
			inArgs.pairsDo{ |key, val|
				val.parMatch({},{|x| (x.size != n).assert("ParArg size % / ParU size % : size mismatch - %".format(x.size,n,val)) });
			};
			args = this.def.asArgsArrayPar( inArgs ? [], this );
		} {
			args = inArgs;
			"def '%' not found".format(in).warn;
		};
		preparedServers = [];
		mod = inMod.asUModFor( this );
		this.changed( \init );
	}

	insertUMap { |key, umapdef, args|
		var item;
		umapdef = umapdef.asUdef( UMapDef );
		if( umapdef.notNil ) {
			//hack
			if( umapdef.category == 'ImmLib' ) {
				if( umapdef.canInsert ) {
					item = this.get( key );
					this.set( key, ImmUMap(surface, umapdef,  args ) );
					this.get( key ).setConstrain( umapdef.insertArgName, item );
				} {
					this.set( key, ImmUMap(surface, umapdef, args ) );
				}
			}{
				if( umapdef.canInsert ) {
					item = this.get( key );
					this.set( key, ParUMap(n, umapdef,  args ) );
					this.get( key ).setConstrain( umapdef.insertArgName, item );
				} {
					this.set( key, ParUMap(n, umapdef, args ) );
				}
			};
		};
	}
}

+ Symbol {
	asImmUnit { |n, surface, args| ^ImmU( surface, this, args ) }
}

+ Array {
	asImmUnit { |n, surface|
		^ImmU(surface, *this)
	}
}

+ U {

	asImmUnit { |n, surface|
		^ImmU(surface, *this.storeArgs)
	}

}
