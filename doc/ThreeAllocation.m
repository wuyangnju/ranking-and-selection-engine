%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%              Three Stage Allocation Problem
%
%                    Aug 30, 2011
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function Throughput=ThreeAllocation(input)

for i=1:5
    x(i)=input(i);
end

WarmupN=2000; % warmup period until first 2000 units have been produced
StopN=3000; % calculate from 2001 to 3000 units
EventN=10^5; % maximum simulated event
Buffer=zeros(3,1); % number of customer in the buffer
Block=zeros(2,1); % record station 1 and 2 being blocked or not

Clock=zeros(EventN,1);

Clock(1)=0;
Eventtype=1;% 1,2,3 represent departure from location 1,2,3

SerTime=zeros(3,1);
SerTime(1)=exprnd(1/x(1));

DepN=0;

i=1;

while(DepN<WarmupN)
    temp=min(SerTime(SerTime>0));
    Eventtype=find(SerTime==temp);
    Clock(i+1)=Clock(i)+temp;
    i=i+1;
    
    if(Eventtype==1)
        if(Buffer(2)==x(4))
            Block(1)=1;
            for j=1:3
                if(SerTime(j)>0)
                    SerTime(j)=SerTime(j)-temp;
                end
            end
        else    
            Block(1)=0;
            SerTime(1)=exprnd(1/x(1));
            if(Block(2)==0)
                if(SerTime(2)>0)                    
                    SerTime(2)=SerTime(2)-temp;
                    Buffer(2)=Buffer(2)+1;
                else
                    SerTime(2)=exprnd(1/x(2));
                end
            else
                Buffer(2)=Buffer(2)+1;
                SerTime(2)=0;
            end
            if(SerTime(3)>0)
                SerTime(3)=SerTime(3)-temp;
            end
        end
    elseif(Eventtype==2)
        if(Buffer(3)==x(5))
            Block(2)=1;
            for j=1:3
                if(SerTime(j)>0)
                    SerTime(j)=SerTime(j)-temp;
                end
            end
        else
            Block(2)=0;
            if(SerTime(3)>0)
                SerTime(3)=SerTime(3)-temp;
                Buffer(3)=Buffer(3)+1;
            else
                SerTime(3)=exprnd(1/x(3));
            end
            if(Block(1)==0)
                SerTime(1)=SerTime(1)-temp;
                if(Buffer(2)>0)
                    Buffer(2)=Buffer(2)-1;
                    SerTime(2)=exprnd(1/x(2));
                else
                    SerTime(2)=0;
                end
            else
                SerTime(1)=exprnd(1/x(1));
                SerTime(2)=exprnd(1/x(2));
                Block(1)=0;
            end
        end
    elseif(Eventtype==3)
        DepN=DepN+1;
        if(Buffer(3)>0)
            SerTime(3)=exprnd(1/x(3));
            Buffer(3)=Buffer(3)-1;
            
            if(Block(2)==0)
                if(SerTime(2)>0)
                    SerTime(2)=SerTime(2)-temp;
                end
                if(SerTime(1)>0)
                    SerTime(1)=SerTime(1)-temp;
                end
            else
                SerTime(2)=exprnd(1/x(2));
                Buffer(3)=Buffer(3)+1;
                Buffer(2)=Buffer(2)-1;
                Block(2)=0;
                if(Block(1)==1)
                    Buffer(2)=Buffer(2)+1;
                    SerTime(1)=exprnd(1/x(1));
                    Block(1)=0;
                else
                    SerTime(1)=SerTime(1)-temp;
                end                    
            end
        else
            SerTime(3)=0;
            if(SerTime(2)>0)
                SerTime(2)=SerTime(2)-temp;
            end
            if(SerTime(1)>0)
                SerTime(1)=SerTime(1)-temp;
            end
        end
        
    end
    
end


StartT=Clock(i);

while(DepN<StopN)
    temp=min(SerTime(SerTime>0));
    Eventtype=find(SerTime==temp);
    Clock(i+1)=Clock(i)+temp;
    i=i+1;
    
    if(Eventtype==1)
        if(Buffer(2)==x(4))
            Block(1)=1;
            for j=1:3
                if(SerTime(j)>0)
                    SerTime(j)=SerTime(j)-temp;
                end
            end
        else    
            Block(1)=0;
            SerTime(1)=exprnd(1/x(1));
            if(Block(2)==0)
                if(SerTime(2)>0)                    
                    SerTime(2)=SerTime(2)-temp;
                    Buffer(2)=Buffer(2)+1;
                else
                    SerTime(2)=exprnd(1/x(2));
                end
            else
                Buffer(2)=Buffer(2)+1;
                SerTime(2)=0;
            end
            if(SerTime(3)>0)
                SerTime(3)=SerTime(3)-temp;
            end
        end
    elseif(Eventtype==2)
        if(Buffer(3)==x(5))
            Block(2)=1;
            for j=1:3
                if(SerTime(j)>0)
                    SerTime(j)=SerTime(j)-temp;
                end
            end
        else
            Block(2)=0;
            if(SerTime(3)>0)
                SerTime(3)=SerTime(3)-temp;
                Buffer(3)=Buffer(3)+1;
            else
                SerTime(3)=exprnd(1/x(3));
            end
            if(Block(1)==0)
                SerTime(1)=SerTime(1)-temp;
                if(Buffer(2)>0)
                    Buffer(2)=Buffer(2)-1;
                    SerTime(2)=exprnd(1/x(2));
                else
                    SerTime(2)=0;
                end
            else
                SerTime(1)=exprnd(1/x(1));
                SerTime(2)=exprnd(1/x(2));
                Block(1)=0;
            end
        end
    elseif(Eventtype==3)
        DepN=DepN+1;
        if(Buffer(3)>0)
            SerTime(3)=exprnd(1/x(3));
            Buffer(3)=Buffer(3)-1;
            
            if(Block(2)==0)
                if(SerTime(2)>0)
                    SerTime(2)=SerTime(2)-temp;
                end
                if(SerTime(1)>0)
                    SerTime(1)=SerTime(1)-temp;
                end
            else
                SerTime(2)=exprnd(1/x(2));
                Buffer(3)=Buffer(3)+1;
                Buffer(2)=Buffer(2)-1;
                Block(2)=0;
                if(Block(1)==1)
                    Buffer(2)=Buffer(2)+1;
                    SerTime(1)=exprnd(1/x(1));
                    Block(1)=0;
                else
                    SerTime(1)=SerTime(1)-temp;
                end                    
            end
        else
            SerTime(3)=0;
            if(SerTime(2)>0)
                SerTime(2)=SerTime(2)-temp;
            end
            if(SerTime(1)>0)
                SerTime(1)=SerTime(1)-temp;
            end
        end
        
    end
end
    
    EndT=Clock(i);
    
    Throughput=(StopN-WarmupN)/(EndT-StartT);
    
    
    
    
    
    
    
    
    
    
    
    
    
    
