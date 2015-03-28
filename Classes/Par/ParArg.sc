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

//just wraps an array
ParArg {
	var <array;

	*new { |array|
		^super.newCopyArgs(array)
	}

	printOn { arg stream;
		stream << this.class.name << "( " << array << " )";
	}

	asParArg{
		^this
	}

	parMatch{ |fnormal, fpar|
		^fpar.(array)
	}

	parCollect{ |f|
		^ParArg(array.collect(f))
	}

	asFloat{
		^array.sum / array.size
	}

	respondsTo { |symbol|
		^array[0].respondsTo(symbol)
	}

	//mimic other args
	/*prepare { |servers, startPos = 0, action|
		if( array[0].respondsTo(\prepare) ) {
			var maction = MultiActionFunc( action );
			array.do({ |i| i.prepare(servers, startPos, maction.getAction) })
		} {
			action.value
		}
	}*/

	disposeFor { |server, unit|
		if( array[0].respondsTo(\disposeFor) ) {
			array.do({ |i| i.disposeFor(server, unit) })
		}
	}

	dispose { | unit|
		if( array[0].respondsTo(\dispose) ) {
			array.do({ |i| i.dispose( unit) })
		}
	}

	cutStart { |amount = 0|
		if( array[0].respondsTo(\cutStart) ) {
			array.do({ |i| i.cutStart(amount) })
		}
	}

	unit_ { |unit|
		if( array[0].respondsTo(\unit_) ) {
			array.do({ |i| i.unit_(unit) })
		}
	}

	disposeSynths {
		if( array[0].respondsTo(\disposeSynths) ) {
			array.do({ |i| i.disposeSynths })
		}
	}

	numChannelsForPlayBuf {
		^array[0].respondsTo(\numChannelsForPlayBuf)
	}

	//check method calls not implemented
	doesNotUnderstand { arg selector ... args;
		[this, selector, args].postln;
		DoesNotUnderstandError(this, selector, args).throw;
	}

	storeArgs{
		^[array]
	}

}
