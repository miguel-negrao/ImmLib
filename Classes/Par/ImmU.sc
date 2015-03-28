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
