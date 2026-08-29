function before(hook, param)
    local uid = param:getVirtualPackageUid(param:getArgument(0))
    if uid == nil then
        return false
    end
    param:setResult(uid)
    return true
end

function after(hook, param)
    local packageName = param:getArgument(0)
    if param:getVirtualPackageUid(packageName) ~= nil then
        return false
    end
    local result = param:getResult()
    if result == nil or param:isPackageAllowed(packageName) then
        return false
    end
    local cls = luajava.bindClass("android.content.pm.PackageManager$NameNotFoundException")
    param:setResult(luajava.new(cls, packageName))
    return true
end
