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

+ MultiUdef {

	asArgsArrayPar { |argPairs, unit, constrain = true|
		var defName, argz, newDefName;
		defName = (argPairs ? []).detectIndex({ |item| item == this.defNameKey });
		if( defName.notNil ) {
			defName = argPairs[ defName + 1 ];
		} {
			defName = udefs[0].name;
		};
		tempDef = this.findUdef( defName );
		argz = tempDef.asArgsArrayPar( argPairs ? [], unit, constrain );
		if( chooseFunc.notNil ) {
			newDefName = chooseFunc.value( argz );
			if( newDefName != defName ) { // second pass
				defName = newDefName;
				tempDef = this.findUdef( defName );
				argz = tempDef.asArgsArrayPar( argPairs ? [], unit, constrain );
			};
		};
		tempDef = nil;
		^argz ++ [ this.defNameKey, defName ];
	}

}

+ Udef {

	asArgsArrayPar { |argPairs, unit, constrain = true|
		argPairs = prepareArgsFunc.value( argPairs ) ? argPairs ? #[];
		^argSpecs.collect({ |item|
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			val = val.parCollect{ |x| x.deepCopy.asUnitArgPar( unit, item.name ) };
			if( constrain && { (val.isKindOf( UMap ) || val.isKindOf( ParUMap ) ).not } ) {
				val = val.parCollect{ |x| item.constrain( x ) }
			};
			[ item.name,  val ]
		}).flatten(1);
	}

	setSynthPar { |unit ...keyValuePairs| //keyValuePairs :: [ keyValueArrays ]n
		[U.synthDict[unit], keyValuePairs].flopWith{ |synths, keyValuePairs2|
			this.prSetSynth( synths, *keyValuePairs2 )
		};
	}

}

+ U {

	asParUnit { |n|
		^ParU(n, *this.storeArgs)
	}

}

/*
this interferes with values that are themselves arrays...
+ Array {
	asParArg{ ^ParArg(this) }
}
*/

+ Object {
	asParArg{ |n| ^ParArg(this.dup(n)) }
	parMatch{ |fnormal, fpar| ^fnormal.(this) }
	parCollect{ |f| ^f.(this) }
	asUnitArgPar { |unit, name| ^this.asUnitArg(unit, name) }
}

+ Symbol {
	asParUnit { |n,args| ^ParU( n, this, args ) }
	asUnitArgPar { |unit, key|
		var umapdef, umap;
		if( unit.getSpec( key ).default.isMemberOf( Symbol ).not ) {
			umapdef = this.asUdef( UMapDef );
			if( unit.canUseUMap( key, umapdef ) ) {
				^ParUMap(unit.n,  this ).asUnitArg( unit, key );
			} {
				^this;
			};
		} {
			^this;
		};
	}
}

+ Array {
	asParArg{ ^ParArg(this) }
	asParUnit { |n|
		^ParU(n, *this)
	}
	asUnitArgPar { |unit, key|
		var umapdef, umap;
		^if( ( this[0].isMemberOf( Symbol ) or: this[0].isKindOf( UMapDef ) ) && {
			this[1].isArray
		} ) {
			umapdef = this[0].asUdef( UMapDef );
			if( umapdef.notNil && { unit.canUseUMap( key, umapdef ) } ) {
				if( umapdef.category == 'ImmLib' ) {
					ImmUMap( unit.surface, *this ).asUnitArg( unit, key )
				} {
					ParUMap( unit.n, *this ).asUnitArg( unit, key )
				};
			} {
				unit.getDefault( key );
			};
		} {
			this;
		};
	}
}
