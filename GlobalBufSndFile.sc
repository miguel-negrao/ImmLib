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