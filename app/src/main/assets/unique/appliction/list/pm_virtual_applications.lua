function after(hook, param)
    local list = param:getResult()
    if list == nil then
        return false
    end
    param:setResult(param:mergeVirtualApplications(list))
    return true
end
