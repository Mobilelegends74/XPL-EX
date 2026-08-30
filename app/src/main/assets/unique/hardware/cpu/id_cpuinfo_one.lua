function before(hook, param)
    --Hook version one a BEFORE Hook
    --We can replace the param to point to the TEMP File 
    --this we attack the lowest in this case libcore.io.IoBridge

    local f = param:getArgument(0)
    if f == nil then
        return false
    end

    local fake = nil
    if string.match(f, "cpuinfo$") then
        local m = param:getArgument(1)
        if m ~= nil then
            log("CPUINFO FLAGS::" .. tostring(m))
        end

        log("App is trying to Open CPUINFO File ... Spoofing....")
        fake = param:createFakeCpuinfoFile()
    elseif f == "/proc/version" or f == "/proc/sys/kernel/osrelease" or f == "/proc/sys/kernel/version" then
        log("App is trying to Open Kernel Info File ... Spoofing....")
        fake = param:createFakeKernelInfoFile(f)
    else
        return false
    end

    if fake == nil then
        log("Error NIL File Object")
        return false
    end

    local path = fake:getPath()
    if path == nil then
        log("Error NIL Path Object")
        return false
    end

    log("Setting Parameter to Point to Path: " .. path)
    param:setArgumentString(0, path)
    return true
end
