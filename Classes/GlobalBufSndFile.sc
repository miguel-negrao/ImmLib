/*
    ImmLib
    Copyright 2013 Miguel Negrao.

    ImmLib: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

GlobalBufSndFile : BufSndFile {
	classvar <bufferAllocator;
	classvar <globalBuffers;

	var <>globalBufNum;

	*initClass {
	    globalBuffers = IdentityDictionary();
		bufferAllocator = ContiguousBlockAllocator(5000);
	}

	*new{ |path, startFrame = 0, endFrame, rate = 1, loop = false, useChannels |
		^super.new(path, startFrame, endFrame, rate, loop, useChannels)
		.globalBufNum_( bufferAllocator.alloc(1) )
	}

    createTheBuffers {
        super.prepare(ULib.allServers)
    }

	buffers { ^globalBuffers[ globalBufNum ] }
	buffers_ { |buffers| globalBuffers[ globalBufNum ] = buffers; }

	prepare {|servers, startPos = 0, action| action.value }
	dispose { }
	disposeFor { }

}

GlobalBufSndFileSpec : BufSndFileSpec {

	*testObject { |obj|
		^obj.isKindOf( GlobalBufSndFile );
	}

}